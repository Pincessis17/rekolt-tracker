package mu.rekolt.service;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.Member;
import mu.rekolt.model.SeasonStore;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Objective 6: writes the season held entirely in SeasonStore's in-memory
 * collections out to output/season-report.docx - one section per member,
 * each starting on a new page, followed by a closing section that
 * reconciles with the member figures. No delivery data is ever read back
 * in from either file this class writes.
 */
public class ReportWriter {

    private static final Path OUTPUT_DIR = Path.of("output");
    private static final Path REPORT_PATH = OUTPUT_DIR.resolve("season-report.docx");
    private static final Path RUN_LOG_PATH = OUTPUT_DIR.resolve("run-log.txt");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] TABLE_HEADERS = {
            "Delivery", "Produce", "Mass (kg)", "Grade", "Commission (MUR)", "Levy (MUR)", "Net payable (MUR)"
    };

    /**
     * Writes the season report and, only once that succeeds, appends the
     * run log line. IOException is left to the caller (Main) to catch and
     * report with an actionable message - this method does not swallow it.
     */
    public static void generateReport(SeasonStore store) throws IOException {
        Files.createDirectories(OUTPUT_DIR);

        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(REPORT_PATH.toFile())) {

            double seasonTotal = 0.0;
            boolean first = true;
            for (Member member : store.getMembers().values()) {
                if (!first) {
                    XWPFParagraph pageBreak = document.createParagraph();
                    pageBreak.setPageBreak(true);
                }
                first = false;
                writeMemberSection(document, member);
                seasonTotal += member.payableAmount();
            }

            writeClosingSection(document, store, seasonTotal);

            document.write(out);
        }

        appendRunLog();
    }

    private static void writeMemberSection(XWPFDocument document, Member member) {
        XWPFParagraph heading = document.createParagraph();
        XWPFRun headingRun = heading.createRun();
        headingRun.setBold(true);
        headingRun.setFontSize(15);
        headingRun.setText(member.getMemberId() + "   " + member.getName());

        List<Delivery> deliveries = member.getDeliveries();

        XWPFTable table = document.createTable(1, TABLE_HEADERS.length);
        setRow(table.getRow(0), TABLE_HEADERS, true, "D9D9D9");

        double commissionTotal = 0.0;
        double levyTotal = 0.0;

        for (Delivery delivery : deliveries) {
            double commission = PaymentCalculator.commissionFor(
                    delivery.getMassKg(), delivery.getProduce(), delivery.getGrade());
            double levy = PaymentCalculator.transportLevyFor(delivery.getMassKg(), delivery.getGrade());
            commissionTotal += commission;
            levyTotal += levy;

            XWPFTableRow row = table.createRow();
            setRow(row, new String[] {
                    delivery.getDeliveryId(),
                    delivery.getProduce().getCode(),
                    String.format("%.1f", delivery.getMassKg()),
                    delivery.getGrade().toString(),
                    money(commission),
                    money(levy),
                    money(delivery.payableAmount())
            }, false, null);
        }

        XWPFTableRow totalRow = table.createRow();
        setRow(totalRow, new String[] {
                "Total", "", "", "", money(commissionTotal), money(levyTotal), money(member.payableAmount())
        }, true, "F2F2F2");

        XWPFParagraph totalParagraph = document.createParagraph();
        XWPFRun totalRun = totalParagraph.createRun();
        totalRun.setBold(true);
        totalRun.setText("Total payable to " + member.getName() + ": " + money(member.payableAmount()) + " MUR");

        XWPFParagraph spacer = document.createParagraph();
        spacer.createRun();

        XWPFParagraph signature = document.createParagraph();
        XWPFRun signatureRun = signature.createRun();
        signatureRun.setText("Signature: ______________________________          Date: ______________");
    }

    private static void writeClosingSection(XWPFDocument document, SeasonStore store, double seasonTotal) {
        XWPFParagraph pageBreak = document.createParagraph();
        pageBreak.setPageBreak(true);

        XWPFParagraph heading = document.createParagraph();
        XWPFRun headingRun = heading.createRun();
        headingRun.setBold(true);
        headingRun.setFontSize(15);
        headingRun.setText("Season totals");

        XWPFParagraph membersLine = document.createParagraph();
        membersLine.createRun().setText("Members this season: " + store.distinctMemberCount());

        XWPFParagraph deliveriesLine = document.createParagraph();
        deliveriesLine.createRun().setText("Deliveries this season: " + store.getDeliveries().size());

        XWPFParagraph totalLine = document.createParagraph();
        XWPFRun totalRun = totalLine.createRun();
        totalRun.setBold(true);
        totalRun.setFontSize(12);
        totalRun.setText("Total net payable, all members: " + money(seasonTotal) + " MUR");

        XWPFParagraph reconcileLine = document.createParagraph();
        XWPFRun reconcileRun = reconcileLine.createRun();
        reconcileRun.setItalic(true);
        reconcileRun.setText("This total is the sum of the \"Total payable\" figure in each member section above.");
    }

    private static void setRow(XWPFTableRow row, String[] values, boolean bold, String shadeHex) {
        for (int i = 0; i < values.length; i++) {
            XWPFTableCell cell = row.getCell(i);
            if (cell == null) {
                cell = row.createCell();
            }
            if (shadeHex != null) {
                cell.setColor(shadeHex);
            }
            cell.removeParagraph(0);
            XWPFParagraph paragraph = cell.addParagraph();
            XWPFRun run = paragraph.createRun();
            run.setBold(bold);
            run.setFontSize(9);
            run.setText(values[i]);
        }
    }

    private static String money(double amount) {
        return String.format("%,.2f", amount);
    }

    private static void appendRunLog() throws IOException {
        String line = LocalDateTime.now().format(TIMESTAMP_FORMAT) + " - season report generated"
                + System.lineSeparator();
        try (java.io.Writer writer = Files.newBufferedWriter(RUN_LOG_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(line);
        }
    }
}
