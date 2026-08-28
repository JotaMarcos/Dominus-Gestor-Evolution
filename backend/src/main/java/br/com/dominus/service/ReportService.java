package br.com.dominus.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Map;

public class ReportService {

    public enum Format {
        PDF, XLSX, CSV, TXT, DOCX
    }

    public static void exportReport(String reportName, Format format, Map<String, Object> params, Connection conn,
            OutputStream out) throws Exception {
        InputStream stream = ReportService.class.getResourceAsStream("/reports/" + reportName + ".jrxml");
        if (stream == null) {
            throw new IllegalArgumentException("Relatório não encontrado: " + reportName);
        }

        JasperPrint jasperPrint;
        try (InputStream reportStream = stream) {
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
        }

        switch (format) {
            case PDF -> JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            case XLSX -> {
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                exporter.exportReport();
            }
            case CSV -> {
                JRCsvExporter exporter = new JRCsvExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleWriterExporterOutput(out));
                exporter.exportReport();
            }
            case DOCX -> {
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                exporter.exportReport();
            }
            case TXT -> {
                JRTextExporter exporter = new JRTextExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleWriterExporterOutput(out));
                exporter.exportReport();
            }
        }
    }
}
