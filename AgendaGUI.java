package agenda;


// Importações estáticas e bibliotecas necessárias para interface, arquivos, rede, etc.
import static agenda.FirebaseService.excluirContato;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableCellRenderer;


// Classe principal da interface gráfica da Agenda
public class AgendaGUI extends JFrame {

    // Componentes da interface gráfica
    private JTable tabela;
    private DefaultTableModel modelo;

    private JTextField unidadeField;
    private JFormattedTextField contatoField;
    private JTextField emailField;
    private JTextField enderecoField;
    private JComboBox<String> categoriaBox;

    private boolean isAdmin;

    
    // Construtor da interface principal
    public AgendaGUI(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setTitle("Agenda de Contatos");
        setSize(1400, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        
        
        // Fontes padronizadas
        java.awt.Font fonteLabel = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
        java.awt.Font fonteCampo = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
        java.awt.Font fonteBotao = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
        
        
        // Modelo da tabela que não permite edição direta
        modelo = new DefaultTableModel(new Object[]{"ID", "Unidade", "Contato", "Email", "Endereço", "Categoria"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Configuração da tabela
        tabela = new JTable(modelo) {
    @Override
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);

        if (rowIndex >= 0 && colIndex == 2) { // Coluna 2 = Contato
            try {
                String unidade = modelo.getValueAt(rowIndex, 1).toString();
                String contato = modelo.getValueAt(rowIndex, 2).toString();
                String email = modelo.getValueAt(rowIndex, 3).toString();
                String endereco = modelo.getValueAt(rowIndex, 4).toString();
                String categoria = modelo.getValueAt(rowIndex, 5).toString();

                return "<html>"
                        + "<b>Unidade:</b> " + unidade + "<br>"
                        + "<b>Contato:</b> " + contato + "<br>"
                        + "<b>Email:</b> " + email + "<br>"
                        + "<b>Endereço:</b> " + endereco + "<br>"
                        + "<b>Categoria:</b> " + categoria
                        + "</html>";
            } catch (Exception ex) {
                return null;
            }
        }

        return null;
    }
};

        tabela.getColumnModel().getColumn(0).setMinWidth(0); // Oculta a coluna ID
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(0).setWidth(0);
        tabela.setFont(fonteCampo);
        tabela.setRowHeight(24);
        tabela.getTableHeader().setFont(fonteLabel.deriveFont(java.awt.Font.BOLD));
        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.setBorder(new EmptyBorder(30, 30, 30, 30)); // top, left, bottom, right
        add(scrollPane, BorderLayout.CENTER);
        
        // Campo de texto para unidade
        unidadeField = new JTextField(30);
        unidadeField.setFont(fonteCampo);
        unidadeField.setPreferredSize(new Dimension(250, 30));
        unidadeField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            // Atualiza lista ao digitar    
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarContatosAoDigitar();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarContatosAoDigitar();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarContatosAoDigitar();
            }
        });
        
        // Campo formatado para telefone
        MaskFormatter telefoneMask = null;
        try {
            telefoneMask = new MaskFormatter("(##) #####-####");
            telefoneMask.setPlaceholderCharacter('_');
        } catch (Exception e) {
            e.printStackTrace();
        }

        contatoField = new JFormattedTextField(telefoneMask);
        contatoField.setColumns(30);
        contatoField.setFont(fonteCampo);
        contatoField.setPreferredSize(new Dimension(250, 30));
                
        // Demais campos
        emailField = new JTextField(30);
        emailField.setFont(fonteCampo);
        emailField.setPreferredSize(new Dimension(250, 30));

        enderecoField = new JTextField(30);
        enderecoField.setFont(fonteCampo);
        enderecoField.setPreferredSize(new Dimension(250, 30));
        
        // ComboBox de categorias
        String[] categorias = {"Sejus", "Sistêmica", "Penal", "Socio"};
        categoriaBox = new JComboBox<>(categorias);
        categoriaBox.setFont(fonteCampo);
        categoriaBox.setPreferredSize(new Dimension(10, 30));
        
        
        // Ações ao trocar a categoria
        categoriaBox.addActionListener(e -> filtrarContatosAoDigitar());
        categoriaBox.addActionListener(e -> {
            if (!isAdmin) {
                filtrarContatosPorCategoria();

            }
        });

        
        // Cria painel com GridBag para campos de dados do contato
        JPanel painelCampos = new JPanel(new GridBagLayout());
        painelCampos.setBorder(new TitledBorder("Detalhes do Contato"));
        painelCampos.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);

        
        // Campos básicos (exibidos sempre)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        painelCampos.add(new JLabel("Unidade:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelCampos.add(unidadeField, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        painelCampos.add(new JLabel("Categoria:"), gbc);

        gbc.gridx = 7;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelCampos.add(categoriaBox, gbc);

        // Campos adicionais apenas para admin
        if (isAdmin) {

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            painelCampos.add(new JLabel("Contato:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            painelCampos.add(contatoField, gbc);

            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            painelCampos.add(new JLabel("Email:"), gbc);

            gbc.gridx = 3;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            painelCampos.add(emailField, gbc);

            gbc.gridx = 4;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            painelCampos.add(new JLabel("Endereço:"), gbc);

            gbc.gridx = 5;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            painelCampos.add(enderecoField, gbc);

        }

        
        // Botões principais
        JButton adicionar = new JButton("Adicionar");
        JButton excluir = new JButton("Excluir");
        JButton gerarPdf = new JButton("Gerar PDF");

        
        // Estilização
        adicionar.setFont(fonteBotao);
        excluir.setFont(fonteBotao);
        gerarPdf.setFont(fonteBotao);

        adicionar.setBackground(new Color(40, 167, 69));
        excluir.setBackground(new Color(220, 53, 69));
        gerarPdf.setBackground(new Color(0, 123, 255));

        adicionar.setForeground(Color.WHITE); //verde
        excluir.setForeground(Color.WHITE); //vermelho
        gerarPdf.setForeground(Color.WHITE); //azul
        
        
        // Painel de botões com alinhamento à direita
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        painelBotoes.setBackground(new Color(245, 245, 245));

        if (isAdmin) {

            painelBotoes.add(adicionar);
            painelBotoes.add(excluir);

        }

        painelBotoes.add(gerarPdf);
        
        // Painel de rodapé com botão de sair e informação do autor

        JPanel painelRodape = new JPanel(new BorderLayout());
        painelRodape.setBackground(new Color(230, 230, 230));

        JButton voltar = new JButton("Sair");
        voltar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        voltar.setBackground(new Color(108, 117, 125));
        voltar.setForeground(Color.WHITE);
        voltar.addActionListener(e -> System.exit(0));

        JPanel painelVoltar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelVoltar.setBackground(new Color(230, 230, 230));
        painelVoltar.add(voltar);

        JLabel rodape = new JLabel("Desenvolvido por Wenderson Novais  |  Versão 2.0");
        rodape.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        rodape.setForeground(new Color(80, 80, 80));
        rodape.setBorder(new EmptyBorder(8, 10, 5, 15));

        painelRodape.add(painelVoltar, BorderLayout.WEST);
        painelRodape.add(rodape, BorderLayout.CENTER);
        
        
            // Junta tudo no painel inferior
        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.setBorder(new EmptyBorder(10, 10, 0, 10));
        painelInferior.setBackground(new Color(245, 245, 245));
        painelInferior.add(painelCampos, BorderLayout.CENTER);
        painelInferior.add(painelBotoes, BorderLayout.SOUTH);
        painelInferior.add(painelRodape, BorderLayout.NORTH);

        add(painelInferior, BorderLayout.SOUTH);

        // Listener para o botão "Adicionar" - cria um novo contato
        adicionar.addActionListener(e -> {
            String unidade = unidadeField.getText().trim();
            String contato = contatoField.getText().trim();
            String email = emailField.getText().trim();
            String endereco = enderecoField.getText().trim();
            String categoria = categoriaBox.getSelectedItem().toString();

            // Valida se os campos obrigatórios estão preenchidos
            if (unidade.isEmpty() || contato.contains("_")) {
                JOptionPane.showMessageDialog(this, "Preencha Todos os Campos Vazios!");
                return;
            }

            try {
                // Cria novo objeto Contato e salva no Firebase
                Contato c = new Contato(null, unidade, contato, email, endereco, categoria);
                FirebaseService.salvarContato(c);
                carregarContatos();// Atualiza a tabela
                
                // Limpa os campos
                unidadeField.setText("");
                contatoField.setText("");
                emailField.setText("");
                enderecoField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        // Atalho Ctrl + C para copiar valor da célula selecionada
        KeyStroke copyKey = KeyStroke.getKeyStroke("ctrl C");
        tabela.getInputMap(JComponent.WHEN_FOCUSED).put(copyKey, "copiarCelula");
        tabela.getActionMap().put("copiarCelula", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                copiarCelulaSelecionada();
            }
        });

        // Criação do menu de contexto (clique direito) com opção "Copiar"
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copiarItem = new JMenuItem("Copiar");
        menu.add(copiarItem);

        copiarItem.addActionListener(e -> copiarCelulaSelecionada());

        // Mostra o menu ao clicar com botão direito na tabela
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    mostrarMenu(e);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    mostrarMenu(e);
                }
            }
            
            // Método auxiliar para exibir o menu no ponto clicado
            private void mostrarMenu(MouseEvent e) {
                int row = tabela.rowAtPoint(e.getPoint());
                int col = tabela.columnAtPoint(e.getPoint());

                if (!tabela.isRowSelected(row)) {
                    tabela.setRowSelectionInterval(row, row);
                }

                if (!tabela.isColumnSelected(col)) {
                    tabela.setColumnSelectionInterval(col, col);
                }

                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        // Listener do botão "Excluir" - remove contato selecionado
        excluir.addActionListener(e -> {
            int row = tabela.getSelectedRow();
            if (row >= 0) {
                String id = modelo.getValueAt(row, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(this, "Deseja excluir este contato?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        excluirContato(id);
                        carregarContatos();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um contato na tabela para excluir.");
            }
        });

         // Listener do botão "Gerar PDF" - exporta os dados da tabela para um arquivo PDF
        gerarPdf.addActionListener(e -> {
            try {
                gerarPdfDaAgenda();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Estiliza a célula da coluna "Contato" como link sublinhado azul
        tabela.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String numero = value.toString().replaceAll("[^0-9]", "");
                    String url = "https://wa.me/55" + numero;

                    cell.setText("<html><a style='color:blue;text-decoration:underline;'>" + value.toString() + "</a></html>");
                    cell.setToolTipText(url); // mostra link ao passar o mouse
                    cell.setCursor(new Cursor(Cursor.HAND_CURSOR)); // deixa mouse como "mãozinha"
                }
                return cell;
            }
        });

        // Listener para abrir WhatsApp Web ao clicar em um número de contato
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabela.rowAtPoint(evt.getPoint());
                int col = tabela.columnAtPoint(evt.getPoint());

                if (col == 2 && row >= 0) {
                    String numeroOriginal = modelo.getValueAt(row, col).toString();
                    String numeroLimpo = numeroOriginal.replaceAll("[^0-9]", "");
                    abrirWhatsApp(numeroLimpo);
                }
            }
        });

        // Muda o cursor para "mãozinha" ao passar o mouse sobre a coluna de contatos
        tabela.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                // Obtém a linha e a coluna sob o cursor do mouse
                int row = tabela.rowAtPoint(evt.getPoint());
                int col = tabela.columnAtPoint(evt.getPoint());

                // Altera o cursor para "mão" se estiver na coluna 2 (índice 2), sugerindo interação
                if (col == 2 && row >= 0) {
                    tabela.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    tabela.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        carregarContatos(); // Carrega os contatos do Firebase ao iniciar
        setVisible(true);  // Exibe a janela da aplicação
    }

    private void abrirWhatsApp(String numero) {
        // Verifica se o número está vazio
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Número inválido.");
            return;
        }

        try {
            
            // Concatena o número com o link do WhatsApp (código do Brasil incluso)
            String url = "https://wa.me/55" + numero;
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir WhatsApp: " + e.getMessage());
        }
    }

    private void copiarCelulaSelecionada() {
        int row = tabela.getSelectedRow();  // Linha selecionada
        int col = tabela.getSelectedColumn(); // Coluna selecionada

        if (row == -1 || col == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma célula para copiar.");
            return;
        }

        Object valor = tabela.getValueAt(row, col); // Obtém o valor da célula
        if (valor != null) {
            // Copia o conteúdo da célula para a área de transferência do sistema

            StringSelection selecao = new StringSelection(valor.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selecao, null);
        }
    }

    private void carregarContatos() {
        try {
            modelo.setRowCount(0); // Limpa a tabela atual
            Map<String, Contato> contatos = FirebaseService.listarContatos(); // Busca do Firebase

            for (Map.Entry<String, Contato> entry : contatos.entrySet()) {
                Contato c = entry.getValue();
                c.setId(entry.getKey()); // Seta o ID do contato
                
                // Adiciona uma linha na tabela com os dados do contato
                modelo.addRow(new Object[]{
                    c.getId(),
                    c.getUnidade(),
                    c.getContato(),
                    c.getEmail(),
                    c.getEndereco(),
                    c.getCategoria() // nova coluna adicionada
                });
            }

            // Ajusta automaticamente o tamanho das colunas da tabela
            new TableColumnAdjuster(tabela).adjustColumns(); // Fora do for
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar contatos: " + e.getMessage());
        }
    }

    private void filtrarContatosAoDigitar() {
        // Obtém os filtros de texto e categoria selecionada
        String filtroTexto = unidadeField.getText().trim().toLowerCase();
        String filtroCategoria = categoriaBox.getSelectedItem().toString().toLowerCase();

        modelo.setRowCount(0); // Limpa a tabela

        try {
            Map<String, Contato> contatos = FirebaseService.listarContatos();

            for (Map.Entry<String, Contato> entry : contatos.entrySet()) {
                Contato c = entry.getValue();
                c.setId(entry.getKey());

                boolean correspondeAoTexto = filtroTexto.isEmpty() || ((c.getUnidade() != null && c.getUnidade().toLowerCase().contains(filtroTexto))
                        || (c.getContato() != null && c.getContato().toLowerCase().contains(filtroTexto))
                        || (c.getEmail() != null && c.getEmail().toLowerCase().contains(filtroTexto))
                        || (c.getEndereco() != null && c.getEndereco().toLowerCase().contains(filtroTexto))
                        || (c.getCategoria() != null && c.getCategoria().toLowerCase().contains(filtroTexto)));

                // Verifica se a categoria selecionada é igual à do contato
                boolean mesmaCategoria = c.getCategoria() != null && c.getCategoria().toLowerCase().equals(filtroCategoria);

                
                // Se passar pelos dois filtros, adiciona o contato na tabela
                if (correspondeAoTexto && mesmaCategoria) {
                    modelo.addRow(new Object[]{
                        c.getId(),
                        c.getUnidade(),
                        c.getContato(),
                        c.getEmail(),
                        c.getEndereco(),
                        c.getCategoria()
                    });
                }
            }

             // Ajusta o tamanho das colunas
            new TableColumnAdjuster(tabela).adjustColumns();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao filtrar contatos: " + e.getMessage());
        }
    }

    private void filtrarContatosPorCategoria() {
        String categoriaSelecionada = categoriaBox.getSelectedItem().toString().toLowerCase();
        modelo.setRowCount(0); // Limpa a tabela

        try {
            Map<String, Contato> contatos = FirebaseService.listarContatos();
            for (Map.Entry<String, Contato> entry : contatos.entrySet()) {
                Contato c = entry.getValue();
                c.setId(entry.getKey());

                 // Verifica se a categoria do contato bate com a categoria selecionada
                if (c.getCategoria() != null
                        && c.getCategoria().toLowerCase().equals(categoriaSelecionada)) {

                    modelo.addRow(new Object[]{
                        c.getId(),
                        c.getUnidade(),
                        c.getContato(),
                        c.getEmail(),
                        c.getEndereco(),
                        c.getCategoria()
                    });
                }
            }

            new TableColumnAdjuster(tabela).adjustColumns();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao filtrar por categoria: " + e.getMessage());
        }
    }

    private void gerarPdfDaAgenda() throws Exception {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Agenda como PDF");

        // Gera nome de arquivo padrão baseado na data e hora
        String nomePadrao = "Agenda_" + java.time.LocalDateTime.now().toString()
                .replace(":", "-")
                .replace(".", "-")
                .replace("T", "_")
                .substring(0, 16) + ".pdf";

        fileChooser.setSelectedFile(new java.io.File(nomePadrao));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File arquivoPdf = fileChooser.getSelectedFile();
        
         // Garante que a extensão .pdf esteja presente
        if (!arquivoPdf.getName().toLowerCase().endsWith(".pdf")) {
            arquivoPdf = new java.io.File(arquivoPdf.getAbsolutePath() + ".pdf");
        }

        // Cria o documento PDF usando iText
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(arquivoPdf));
        document.open();

        // Título centralizado
        Paragraph titulo = new Paragraph("Agenda de Contatos",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK));
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(Chunk.NEWLINE);

        // Criação da tabela PDF, ignorando a primeira coluna (ID)
        PdfPTable pdfTable = new PdfPTable(tabela.getColumnCount() - 1);
        pdfTable.setWidthPercentage(100);
        pdfTable.setSpacingBefore(10f);
        pdfTable.setSpacingAfter(10f);

        // Cabeçalhos
        for (int i = 1; i < tabela.getColumnCount(); i++) {
            PdfPCell cell = new PdfPCell(new Phrase(tabela.getColumnName(i),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfTable.addCell(cell);
        }

        // Conteúdo da tabela
        for (int row = 0; row < tabela.getRowCount(); row++) {
            for (int col = 1; col < tabela.getColumnCount(); col++) {
                Object value = tabela.getValueAt(row, col);
                pdfTable.addCell(value != null ? value.toString() : "");
            }
        }

        document.add(pdfTable);
        document.close();

         // Abre o PDF automaticamente, se suportado
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(arquivoPdf);
        }
    }
}
