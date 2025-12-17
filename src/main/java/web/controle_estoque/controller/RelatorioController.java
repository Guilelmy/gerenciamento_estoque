package web.controle_estoque.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import web.controle_estoque.service.RelatorioService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    // 1. Relatório Agrupado por Fornecedor
    @GetMapping("/por-fornecedor")
    public void gerarRelatorioPorFornecedor(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String headerValue = "attachment; filename=estoque_por_fornecedor_" + dataHora + ".pdf";
        response.setHeader("Content-Disposition", headerValue);

        relatorioService.exportarPdfEstoquePorFornecedor(response);
    }

    // 2. Relatório Geral (Tabela Colorida)
    @GetMapping("/geral")
    public void gerarRelatorioGeral(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String headerValue = "attachment; filename=estoque_geral_" + dataHora + ".pdf";
        response.setHeader("Content-Disposition", headerValue);

        relatorioService.exportarPdfEstoqueGeral(response);
    }
}