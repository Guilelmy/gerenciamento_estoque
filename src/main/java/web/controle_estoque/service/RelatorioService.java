package web.controle_estoque.service;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import web.controle_estoque.model.Empresa;
import web.controle_estoque.model.Fornecedor;
import web.controle_estoque.model.ItemEstoque;
import web.controle_estoque.model.Produto;
import web.controle_estoque.repository.FornecedorRepository;
import web.controle_estoque.repository.ItemEstoqueRepository;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final FornecedorRepository fornecedorRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final SecurityService securityService;

    // ==================================================================================
    // RELATÓRIO 1: ESTOQUE AGRUPADO POR FORNECEDOR (Mestre-Detalhe)
    // ==================================================================================
    @Transactional
    public void exportarPdfEstoquePorFornecedor(HttpServletResponse response) throws IOException {
        Empresa empresa = securityService.getEmpresaLogada();
        
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Cabeçalho
        adicionarCabecalho(document, empresa, "Relatório de Produtos por Fornecedor");

        // Corpo
        List<Fornecedor> fornecedores = fornecedorRepository.findByEmpresaIdAndAtivoTrue(empresa.getId());

        if (fornecedores.isEmpty()) {
            document.add(new Paragraph("Nenhum fornecedor cadastrado."));
        } else {
            for (Fornecedor f : fornecedores) {
                // 1. Mestre (Fornecedor)
                PdfPTable tabelaMestre = new PdfPTable(1);
                tabelaMestre.setWidthPercentage(100f);
                tabelaMestre.setSpacingBefore(10);
                
                PdfPCell celulaFornecedor = new PdfPCell(new Phrase("Fornecedor: " + f.getNome() + " (CNPJ: " + f.getCnpj() + ")", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
                celulaFornecedor.setBackgroundColor(Color.DARK_GRAY);
                celulaFornecedor.setPadding(5);
                tabelaMestre.addCell(celulaFornecedor);
                document.add(tabelaMestre);

                // 2. Detalhe (Produtos)
                List<Produto> produtos = f.getProdutos(); 
                
                if (produtos == null || produtos.isEmpty()) {
                    Paragraph p = new Paragraph("   Nenhum produto vinculado.", FontFactory.getFont(FontFactory.HELVETICA, 10));
                    p.setIndentationLeft(20);
                    document.add(p);
                } else {
                    PdfPTable tabelaDetalhe = new PdfPTable(3); 
                    tabelaDetalhe.setWidthPercentage(95f);
                    tabelaDetalhe.setWidths(new float[] { 3f, 1.5f, 4f });
                    tabelaDetalhe.setSpacingBefore(5);
                    tabelaDetalhe.setSpacingAfter(15);

                    adicionarCabecalhoTabela(tabelaDetalhe, "Produto", "Preço", "Descrição");

                    for (Produto p : produtos) {
                        if (p.isAtivo()) {
                            tabelaDetalhe.addCell(new Phrase(p.getNome(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
                            tabelaDetalhe.addCell(new Phrase(formatarMoeda(p.getPreco()), FontFactory.getFont(FontFactory.HELVETICA, 10)));
                            tabelaDetalhe.addCell(new Phrase(p.getDescricao() != null ? p.getDescricao() : "-", FontFactory.getFont(FontFactory.HELVETICA, 10)));
                        }
                    }
                    document.add(tabelaDetalhe);
                }
            }
        }
        document.close();
    }

    // ==================================================================================
    // RELATÓRIO 2: GERAL COM STATUS (Tabela Colorida)
    // ==================================================================================
    public void exportarPdfEstoqueGeral(HttpServletResponse response) throws IOException {
        Empresa empresa = securityService.getEmpresaLogada();
        List<ItemEstoque> estoque = itemEstoqueRepository.findByEmpresaId(empresa.getId());

        Document document = new Document(PageSize.A4.rotate()); 
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Cabeçalho
        adicionarCabecalho(document, empresa, "Relatório Geral de Estoque");

        // Tabela
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] { 3.5f, 3f, 1f, 1.5f, 1.5f, 1f, 1.5f });

        String[] headers = {"Produto", "Fornecedor", "Qtd", "Unitário", "Total", "Mín", "Status"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        BigDecimal valorTotalGeral = BigDecimal.ZERO;
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        for (ItemEstoque item : estoque) {
            BigDecimal preco = item.getProduto().getPreco();
            BigDecimal totalItem = preco.multiply(new BigDecimal(item.getQuantidadeAtual()));
            valorTotalGeral = valorTotalGeral.add(totalItem);

            table.addCell(new Phrase(item.getProduto().getNome(), fontNormal));
            table.addCell(new Phrase(item.getProduto().getFornecedor().getNome(), fontNormal));
            
            PdfPCell cellQtd = new PdfPCell(new Phrase(String.valueOf(item.getQuantidadeAtual()), fontNormal));
            cellQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellQtd);

            PdfPCell cellPreco = new PdfPCell(new Phrase(formatarMoeda(preco), fontNormal));
            cellPreco.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cellPreco);

            PdfPCell cellTotal = new PdfPCell(new Phrase(formatarMoeda(totalItem), fontNormal));
            cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotal.setBackgroundColor(new Color(240, 240, 240));
            table.addCell(cellTotal);

            PdfPCell cellMin = new PdfPCell(new Phrase(String.valueOf(item.getQuantidadeMinima()), fontNormal));
            cellMin.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellMin);

            table.addCell(criarCelulaStatus(item));
        }

        document.add(table);

        Paragraph pTotal = new Paragraph("Valor Total do Estoque: " + formatarMoeda(valorTotalGeral), 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        pTotal.setSpacingBefore(10);
        document.add(pTotal);

        document.close();
    }

    // ==================================================================================
    // MÉTODOS AUXILIARES
    // ==================================================================================

    private void adicionarCabecalho(Document document, Empresa empresa, String tituloTexto) throws DocumentException {
        Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

        Paragraph pNomeEmpresa = new Paragraph(empresa.getNome().toUpperCase(), fontEmpresa);
        pNomeEmpresa.setAlignment(Element.ALIGN_CENTER);
        document.add(pNomeEmpresa);

        Paragraph pDadosEmpresa = new Paragraph(
            "CNPJ: " + empresa.getCnpj() + " | E-mail: " + empresa.getEmail() + "\n" +
            tituloTexto + " - Gerado em: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
            fontDados);
        pDadosEmpresa.setAlignment(Element.ALIGN_CENTER);
        pDadosEmpresa.setSpacingAfter(20);
        document.add(pDadosEmpresa);
    }

    private void adicionarCabecalhoTabela(PdfPTable table, String... colunas) {
        for (String coluna : colunas) {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(Color.LIGHT_GRAY);
            header.setBorderWidth(2);
            header.setPhrase(new Phrase(coluna, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            header.setPadding(4);
            table.addCell(header);
        }
    }

    private PdfPCell criarCelulaStatus(ItemEstoque item) {
        String texto;
        Color corTexto;
        
        if (item.getQuantidadeAtual() <= item.getQuantidadeMinima()) {
            texto = "CRÍTICO";
            corTexto = Color.RED;
        } else if (item.getQuantidadeAtual() <= (item.getQuantidadeMinima() * 1.2)) {
            texto = "ATENÇÃO";
            corTexto = new Color(204, 153, 0); 
        } else {
            texto = "OK";
            corTexto = new Color(0, 128, 0); 
        }

        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, corTexto)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
    
    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) return "R$ 0,00";
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }
}