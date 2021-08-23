package view;

import classes.Agente;
import classes.Gerenciador;
import classes.Vacina;
import classes.Vacinacao;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @version 1.0
 * @author Matheus Felipe Lima
 */
public class Interface extends javax.swing.JFrame {

    Image icon;
    DefaultTableModel table;
    DefaultTableModel tableVacina;
    DefaultTableModel tableVacinacao;
    DefaultTableModel tableVacinadosPorFabricante;
    DefaultTableModel tableVacinadosPorIdade;
    Gerenciador ger = new Gerenciador();
    Agente agente;
    Vacina vacina;
    Vacinacao vacinacao;
    BufferedImage imagem;
    int vacinaSelectRow;
    int agenteSelectRow;
    int vacinacaoSelectRow;
    
    public Interface() {
        FlatLightLaf.install();
        initComponents();
        icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/imagens/coronavirus.png"));
        setIconImage(icon);
        table = (DefaultTableModel) agentes.getModel();
        tableVacina = (DefaultTableModel) vacinas.getModel();
        tableVacinacao = (DefaultTableModel) vacinacoes.getModel();
        tableVacinadosPorFabricante = (DefaultTableModel) vacinasUsadas.getModel();
        tableVacinadosPorIdade = (DefaultTableModel) vacinadosPorIdade.getModel();
        quantidadeVacina.setDocument(new onlyNum());
        dataNascimentoVacinacao.setDateFormatString("dd/MM/yyyy");
    }
    
    /**
     * Método que tenta importar os dados da db e, se não conseguir, exibe um JOptionPane informando o errro e perguntando se o usuário deseja tentar novamente ou sair.
     */
    private void abrirJanela() {
        if(ger.importarDBAgentes()&&ger.importarDBVacinacao()&&ger.importarDBVacinas()){
            ger.atualizarTableAgentes(table);
            ger.atualizarTableVacinas(tableVacina);
            ger.atualizarTableVacinacao(tableVacinacao);
            carregando.setVisible(false);
            if (ger.getListaAgentes().size() == 0) {
                ger.cadastrarAgenteSemFoto("Admin", "000.000.000-00", "Masculino", true, "admin");
                JOptionPane.showMessageDialog(this, "Por algum motivo a db de agente foi excluída. Isso pode ter se dado por mau uso ou muitos usuários utilizando ao mesmo tempo. Mas já adicionamos o login padrão!");
            }
        } else {
            Object[] options = {"Tentar novamente", "Sair"};
            int escolha = JOptionPane.showOptionDialog(this, "A conexão com o banco de dados pode estar falha ou você pode estar sem internet. O que deseja fazer?", "Falha na comunicação", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if(escolha==0){
                abrirJanela();
            } else {
                this.dispose();
            }
        }
    }
    
    /**
     * Método tenta exportar os dados pra db e fechar o programa, caso não consiga, avisa ao usuário sobre o erro e se ele deseja tentar novamente ou sair sem salvar.
     */
    public void fecharJanela(){
        if(ger.exportarDBAgentes()&&ger.exportarDBVacinacao()&&ger.exportarDBVacinas()){
            this.dispose();
        } else {
            Object[] options = {"Tentar novamente", "Sair mesmo assim"};
            int escolha = JOptionPane.showOptionDialog(this, "Tivemos um probleminha em mandar os dados para o banco de dados. A conexão com servidor pode estar falha ou você sem internet. O que deseja fazer? Caso saia mesmo assim parte dos novos dados que cadastrou pode ser perdido.", "Falha na comunicação", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if(escolha==0){
                fecharJanela();
            } else {
                this.dispose();
            }
        }
    }
    
    /**
     * Método requisita que o usuario preencha todos os campos, chama o método login do gerenciador para verificar se existe no ArrayList listaAgentes e, se existir, verifica se tem permissão admin para liberar a aba com as respectivas funcionalidades
     */
    public void entrar() {
        String senha = String.valueOf(senhaLogin.getPassword());

        //necessario preencher os dois campos
        if (senha.equals("") || cpfLogin.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos (CPF e Senha) para acessar o sistema", "Erro", JOptionPane.ERROR_MESSAGE);
            cpfLogin.setText("");
            senhaLogin.setText("");
        } else if (ger.login(cpfLogin.getText(), senha)) {
            nome.setText(ger.consultarAgente(cpfLogin.getText()).getNome());
            trocarAba(menu);
            if (ger.consultarAgente(cpfLogin.getText()).getPermissao() == false) {
                registroAgente.setVisible(false);
                registroVacina.setVisible(false);
            } else {
                registroAgente.setVisible(true);
                registroVacina.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "CPF ou senha incorreta. Tente novamente!", "Erro", JOptionPane.ERROR_MESSAGE);
            cpfLogin.setText("");
            senhaLogin.setText("");
        }
    }
    
    /**
     * Atualiza a table, em dados gerais, que mostra a quantidade de pessoas vacinadas por fabricante
     */
    public void atualizarTablePorFabricante(){
        tableVacinadosPorFabricante.setRowCount(0);
        for(Vacina vacina: ger.getListaVacinas()){
            String vac = vacina.getNome()+", lote "+vacina.getLote();
            int i = 0;
            for(Vacinacao vacinacao: ger.getListaVacinacao()){
                if(vac.equals(vacinacao.getVacinaLote())){
                    i++;
                }
            }
            tableVacinadosPorFabricante.addRow(new Object[] {vacina.getNome(),vacina.getLote(),i});
        }
    }
    
    /**
     * Atualiza a table em dados gerais que informa a quantidade de pessoas vacinadas por faixa etária
     */
    public void vacinadosPorIdade(){
        tableVacinadosPorIdade.setRowCount(0);
        int dez = 0, trinta = 0, cinquenta = 0, setenta = 0, noventa = 0;
        for(Vacinacao vacinacao: ger.getListaVacinacao()){
            if(Integer.parseInt(getIdade(vacinacao.getDataNasc()))>=10&&Integer.parseInt(getIdade(vacinacao.getDataNasc()))<30){
                dez++;
            }
            if(Integer.parseInt(getIdade(vacinacao.getDataNasc()))>=30&&Integer.parseInt(getIdade(vacinacao.getDataNasc()))<50){
                trinta++;
            }
            if(Integer.parseInt(getIdade(vacinacao.getDataNasc()))>=50&&Integer.parseInt(getIdade(vacinacao.getDataNasc()))<70){
                cinquenta++;
            }
            if(Integer.parseInt(getIdade(vacinacao.getDataNasc()))>=70&&Integer.parseInt(getIdade(vacinacao.getDataNasc()))<90){
                setenta++;
            }
            if(Integer.parseInt(getIdade(vacinacao.getDataNasc()))>90){
                noventa++;
            }
        }
        tableVacinadosPorIdade.addRow(new Object[] {dez,trinta,cinquenta,setenta,noventa});
    }

    /**
     * Limpa os campos do registro de vacina
     */
    private void limparCamposVacina() {
        numVacinas.setText(ger.getListaVacinas().size() + "");
        nomeVacina.setText("");
        loteVacina.setText("");
        quantidadeVacina.setText("");
        intervaloVacina.setText("");
        intervalo1.setText("");
        intervalo2.setText("");
        ger.atualizarTableVacinas(tableVacina);
        pesquisaVacina.setText("");
    }

    /**
     * Limpa os campos do registro de agente
     */
    private void limparCamposAgente() {
        numAgentes.setText(ger.getListaAgentes().size() + "");
        nomeAgente.setText("");
        cpfAgente.setText("");
        senhaAgente.setText("");
        permissao.setSelected(false);
        pesquisarAgente.setText("");
        ger.atualizarTableAgentes(table);
    }

    /**
     * Atualiza a JComboBox de vacinas disponíveis em controle de vacinação. Apenas as vacinas que tem estoque de vacina disponível será mostrado.
     */
    private void atualizarJComboBoxVacinasDisponiveis() {
        vacinasDisponiveis.removeAllItems();
        atualizarTablePorFabricante();
        for(int i=0;i<ger.getListaVacinas().size();i++){
            int qtidadeVacina = Integer.parseInt(ger.getListaVacinas().get(i).getQuantidade());
            int vacinasUsadas = (int)tableVacinadosPorFabricante.getValueAt(i, 2);
            if(qtidadeVacina>vacinasUsadas){
            vacinasDisponiveis.addItem(ger.getListaVacinas().get(i).getNome()+", lote "+ger.getListaVacinas().get(i).getLote());
            }
        }
    }
    
    /**
     * Limpa os campos nome, segunda dose, data nasc. e cpf e atualiza o número de vacinados.
     */
    private void limparCamposVacinacao() {
        numVacinados.setText(ger.getListaVacinacao().size()+"");
        nomeVacinacao.setText("");
        segundaDoseVacinacao.setSelectedIndex(0);
        dataNascimentoVacinacao.setDate(null);
        cpfVacinacao.setText("");
        pesquisarVacinacao.setText("");
        intervalo1v.setText("");
        intervalo2v.setText("");
        ger.atualizarTableVacinacao(tableVacinacao);
    }
    
    /**
     * Pega a data do JDateChooser e converte para o formato dd/MM/yyyy
     * @param data
     * @return data formatada ou, caso a data seja inválida, null
     */
    public String dateToStringFormatado(Date data) {
        try {
            SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM/yyyy");
            
            String dataFormatada = formatoDesejado.format(data);
            
            return dataFormatada;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Data inválida");
            dataNascimentoVacinacao.setDate(null);
            return null;
        }
    }
    
    /**
     * Pega os dados de nascimento e o converte para idade
     * @param dataNasc
     * @return idade
     */
    public static String getIdade(String dataNasc) {

        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dataNascInput = null;
        try {
            dataNascInput = sdf.parse(dataNasc);
        } catch (ParseException ex) {
            
        }
        Calendar dt = new GregorianCalendar();
        try {
            dt.setTime(dataNascInput);
        } catch (Exception e) {
        }
        // Cria um objeto calendar com a data atual
        Calendar today = Calendar.getInstance();
        // Obtém a idade baseado no ano
        int age = today.get(Calendar.YEAR) - dt.get(Calendar.YEAR);
        dt.add(Calendar.YEAR, age);
        if (today.before(dt)) {
            age--;
        }
        return age+"";
    }

    /**
     * Preenche os campos de vacinação de acordo com uma linha da table.
     */
    private void preencherCamposVacinacao() {
        nomeVacinacao.setText((String)tableVacinacao.getValueAt(vacinacoes.getSelectedRow(), 0));
        segundaDoseVacinacao.setSelectedItem(tableVacinacao.getValueAt(vacinacoes.getSelectedRow(), 7));
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date data = formato.parse(tableVacinacao.getValueAt(vacinacoes.getSelectedRow(), 2)+"");
            dataNascimentoVacinacao.setDate(data);
        } catch (ParseException ex) {
            //a data nunca vem em formato diferente do estipulado da tabela, pois ela ja passa por essa verificação ao entrar na tabela, logo, nunca cairá na exceção
        }
        vacinasDisponiveis.setSelectedItem(tableVacinacao.getValueAt(vacinacoes.getSelectedRow(), 6));
        cpfVacinacao.setText((String)tableVacinacao.getValueAt(vacinacoes.getSelectedRow(), 1));
    }


    public class onlyNum extends PlainDocument {
        /**
         * Faz com que o campo só aceite números.
         * @param offs
         * @param str
         * @param a
         * @throws BadLocationException 
         */
        @Override
        public void insertString (int offs, String str, AttributeSet a) throws BadLocationException {
            super.insertString(offs, str.replaceAll("[^0-1-2-3-4-5-6-7-8-9^]", ""), a);
        }
    }
    
    /**
     * Preenche os campos de agente de acordo com a linha clicada na table.
     */
    public void preencherCamposAgentes(){
        nomeAgente.setText((String)agentes.getModel().getValueAt(agentes.getSelectedRow(), 0));
        cpfAgente.setText((String)agentes.getModel().getValueAt(agentes.getSelectedRow(), 1));
        String sexo = (String) agentes.getModel().getValueAt(agentes.getSelectedRow(), 2);
        switch (sexo) {
            case "Masculino":
                sexoAgente.setSelectedIndex(0);
                break;
            case "Feminino":
                sexoAgente.setSelectedIndex(1);
                break;
            default:
                sexoAgente.setSelectedIndex(2);
                break;
        }
        senhaAgente.setText(ger.getListaAgentes().get(agentes.getSelectedRow()).getSenha());
        permissao.setSelected((boolean)agentes.getModel().getValueAt(agentes.getSelectedRow(), 3));
        exibiImagemLabel(ger.getListaAgentes().get(agentes.getSelectedRow()).getFotoPerfil(), fotoPerfil);
    }
   
    /**
     * Preenche os campos de vacina de acordo com a linha clicada na table.
     */
    public void preencherCamposVacinas(){
        nomeVacina.setText((String) tableVacina.getValueAt(vacinas.getSelectedRow(), 0));
        loteVacina.setText((String) tableVacina.getValueAt(vacinas.getSelectedRow(), 1));
        quantidadeVacina.setText((String) vacinas.getModel().getValueAt(vacinas.getSelectedRow(), 2));
        intervaloVacina.setText((String) vacinas.getModel().getValueAt(vacinas.getSelectedRow(), 4));
    }
    
    /**
     * Troca o JPanel.
     * @param aba 
     */
    public void trocarAba(JPanel aba){
        grupo.removeAll();
        grupo.add(aba);
        grupo.repaint();
        grupo.revalidate();
    }
    
    /**
     * Pega a data atual e formata para dd/MM/uuuu
     * @return data formatada
     */
    public String getData(){
        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        String dataFormatada = formatterData.format(agora);
        return dataFormatada;
    }
    
    /**
     * Adiciona o intervalo da vacina à data atual.
     * @return data formatada de retorno ou, caso o retorno seja "00 dias", retorna "Sem retorno"
     */
    public String getRetorno() {
        for (Vacina m : ger.getListaVacinas()) {
            if (vacinasDisponiveis.getSelectedItem().toString().equals(m.getNome() + ", lote " + m.getLote()) && (!m.getIntervalo().equals("00 dias"))) {

                //adiciona o intervalo "x dias" a data atual; substring oculta o " dias"
                LocalDate retorno = LocalDate.now().plusDays(Long.parseLong(m.getIntervalo().substring(0, 2)));
                DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/uuuu");

                return formatterData.format(retorno);
            }
        }
        return "Sem retorno";
    }
    
    /** Método que redimensiona a imagem de acordo com os parâmetros passados
     * @param caminhoImg
     * @param imgLargura
     * @param imgAltura
     * @return imagem redimensionada
     */
    public BufferedImage setImagemDimensao(String caminhoImg, Integer imgLargura, Integer imgAltura) {
        Double novaImgLargura;
        Double novaImgAltura;
        Double imgProporcao;
        Graphics2D g2d;
        BufferedImage imagem = null, novaImagem;

        try {
            // Obtém a imagem a ser redimensionada 
            imagem = ImageIO.read(new File(caminhoImg));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        // Obtém a largura da imagem   
        novaImgLargura = (double) imagem.getWidth();

        //Obtám a altura da imagem   
        novaImgAltura = (double) imagem.getHeight();

    // Verifica se a altura ou largura da imagem recebida é maior do que os   
        // parâmetros de altura e largura recebidos para o redimensionamento     
        if (novaImgLargura >= imgLargura) {
            imgProporcao = (novaImgAltura / novaImgLargura);//calcula a proporção  
            novaImgLargura = (double) imgLargura;

            // altura deve <= ao parâmetro imgAltura e proporcional a largura   
            novaImgAltura = (novaImgLargura * imgProporcao);

        // se altura for maior do que o parâmetro imgAltura, diminui-se a largura de   
            // forma que a altura seja igual ao parâmetro imgAltura e proporcional a largura   
            while (novaImgAltura > imgAltura) {
                novaImgLargura = (double) (--imgLargura);
                novaImgAltura = (novaImgLargura * imgProporcao);
            }
        } else if (novaImgAltura >= imgAltura) {
            imgProporcao = (novaImgLargura / novaImgAltura);//calcula a proporção  
            novaImgAltura = (double) imgAltura;

        // se largura for maior do que o parâmetro imgLargura, diminui-se a altura de   
            // forma que a largura seja igual ao parâmetro imglargura e proporcional a altura   
            while (novaImgLargura > imgLargura) {
                novaImgAltura = (double) (--imgAltura);
                novaImgLargura = (novaImgAltura * imgProporcao);
            }
        }

        novaImagem = new BufferedImage(novaImgLargura.intValue(), novaImgAltura.intValue(), BufferedImage.TYPE_INT_RGB);
        g2d = novaImagem.createGraphics();
        g2d.drawImage(imagem, 0, 0, novaImgLargura.intValue(), novaImgAltura.intValue(), null);

        return novaImagem;
    }

    /**
     * Transforma a imagem do formato BufferedImage para byte[]
     * @param image
     * @return imagem no formato byte[]
     */
    public byte[] getImgBytes(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "JPEG", baos);
        } catch (IOException ex) {
            
        }

        InputStream is = new ByteArrayInputStream(baos.toByteArray());

        return baos.toByteArray();
    }

    /**
     * Método exibe a imagem em byte[] convertida em inputstream, formato reconhecido pelo ImageIO e a exibe na label
     * @param minhaimagem
     * @param label 
     */
    public void exibiImagemLabel(byte[] minhaimagem, javax.swing.JLabel label) {
        //primeiro verifica se tem a imagem
        //se tem convert para inputstream que é o formato reconhecido pelo ImageIO

        if (minhaimagem != null) {
            InputStream input = new ByteArrayInputStream(minhaimagem);
            try {
                BufferedImage imagem = ImageIO.read(input);
                label.setIcon(new ImageIcon(imagem));
            } catch (IOException ex) {
            }
        } else {
            label.setIcon(null);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupo = new javax.swing.JPanel();
        login = new javax.swing.JPanel();
        carregando = new javax.swing.JPanel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        IMUNA = new javax.swing.JLabel();
        cpfLogin = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter cpf= new javax.swing.text.MaskFormatter("###.###.###-##");
            cpfLogin = new javax.swing.JFormattedTextField(cpf);
        }
        catch (Exception e){
        }
        senhaLogin = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnEntrar = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        dados_gerais = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        vacinasUsadas = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        numAgentesCadastrados = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        numTotalVacinados = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        numVacinadosSegundaDose = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        vacinadosPorIdade = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        totalVacinas = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        vacinasEmEstoque = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel48 = new javax.swing.JLabel();
        registrarAgente = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        nomeAgente = new javax.swing.JTextField();
        cpfAgente = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter cpf= new javax.swing.text.MaskFormatter("###.###.###-##");
            cpfAgente = new javax.swing.JFormattedTextField(cpf);
        }catch (Exception e){
        }
        add_editar_agente = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        permissao = new javax.swing.JCheckBox();
        limpar_excluir_agente = new javax.swing.JButton();
        senhaAgente = new javax.swing.JPasswordField();
        removerFoto = new javax.swing.JButton();
        sexoAgente = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        fotoPerfil = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        agentes = new javax.swing.JTable();
        jButton7 = new javax.swing.JButton();
        pesquisarAgente = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        numAgentes = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        sobre = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        logo = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        registrarVacina = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        nomeVacina = new javax.swing.JTextField();
        loteVacina = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter cpf= new javax.swing.text.MaskFormatter("###.###.###-##");
            cpfAgente = new javax.swing.JFormattedTextField(cpf);
        }
        catch (Exception e){
        }
        add_editar_vacina = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        limpar_excluir_vacina = new javax.swing.JButton();
        quantidadeVacina = new javax.swing.JTextField();
        intervaloVacina = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter xDias= new javax.swing.text.MaskFormatter("## dias");
            intervaloVacina = new javax.swing.JFormattedTextField(xDias);
        }
        catch (Exception e){
        }
        jScrollPane2 = new javax.swing.JScrollPane();
        vacinas = new javax.swing.JTable();
        jButton10 = new javax.swing.JButton();
        pesquisaVacina = new javax.swing.JTextField();
        numVacinas = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        intervalo1 = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter dnv= new javax.swing.text.MaskFormatter("##/##/####");
            intervalo1 = new javax.swing.JFormattedTextField(dnv);
        }catch (Exception e){
        }
        intervalo2 = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter dnv= new javax.swing.text.MaskFormatter("##/##/####");
            intervalo2 = new javax.swing.JFormattedTextField(dnv);
        }catch (Exception e){
        }
        jLabel54 = new javax.swing.JLabel();
        porIntervaloVacina = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        menu = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jLabel33 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        nome = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        dadosGerais = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        controleVacinacao = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        registroAgente = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        registroVacina = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        registrarVacinacao = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        nomeVacinacao = new javax.swing.JTextField();
        cpfVacinacao = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter cpf= new javax.swing.text.MaskFormatter("###.###.###-##");
            cpfVacinacao = new javax.swing.JFormattedTextField(cpf);
        }catch (Exception e){
        }
        jLabel25 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        vacinasDisponiveis = new javax.swing.JComboBox<>();
        jLabel39 = new javax.swing.JLabel();
        add_editar_vacinacao = new javax.swing.JButton();
        limpar_excluir_vacinacao = new javax.swing.JButton();
        jLabel42 = new javax.swing.JLabel();
        segundaDoseVacinacao = new javax.swing.JComboBox<>();
        dataNascimentoVacinacao = new com.toedter.calendar.JDateChooser();
        jButton1 = new javax.swing.JButton();
        pesquisarVacinacao = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        vacinacoes = new javax.swing.JTable();
        numVacinados = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        porIntervaloVacinacao = new javax.swing.JButton();
        intervalo1v = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter dnv= new javax.swing.text.MaskFormatter("##/##/####");
            intervalo1v = new javax.swing.JFormattedTextField(dnv);
        }catch (Exception e){
        }
        jLabel55 = new javax.swing.JLabel();
        intervalo2v = new javax.swing.JTextField();
        try{
            javax.swing.text.MaskFormatter dnv= new javax.swing.text.MaskFormatter("##/##/####");
            intervalo2v = new javax.swing.JFormattedTextField(dnv);
        }catch (Exception e){
        }
        jButton8 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("IMUNA");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fecharWindow(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                abrirWindow(evt);
            }
        });

        grupo.setLayout(new java.awt.CardLayout());

        login.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                loginMousePressed(evt);
            }
        });
        login.setLayout(null);

        carregando.setBackground(new java.awt.Color(0, 124, 146));
        carregando.setLayout(null);

        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(208, 216, 218));
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel44.setText("Estamos preparando tudo para você!");
        carregando.add(jLabel44);
        jLabel44.setBounds(0, 120, 500, 50);

        jLabel45.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(208, 216, 218));
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel45.setText("Aguarde...");
        carregando.add(jLabel45);
        jLabel45.setBounds(0, 90, 500, 50);

        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/loading (1).png"))); // NOI18N
        carregando.add(jLabel46);
        jLabel46.setBounds(20, 160, 460, 60);

        jPanel5.setBackground(new java.awt.Color(0, 154, 187));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        carregando.add(jPanel5);
        jPanel5.setBounds(0, 40, 520, 50);

        jPanel9.setBackground(new java.awt.Color(0, 154, 187));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        carregando.add(jPanel9);
        jPanel9.setBounds(0, 240, 520, 50);

        jPanel10.setBackground(new java.awt.Color(0, 154, 187));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        carregando.add(jPanel10);
        jPanel10.setBounds(0, 20, 520, 10);

        jPanel11.setBackground(new java.awt.Color(0, 154, 187));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        carregando.add(jPanel11);
        jPanel11.setBounds(0, 220, 520, 10);

        login.add(carregando);
        carregando.setBounds(0, 250, 510, 280);

        IMUNA.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        IMUNA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/IMUNALOGO-removebg-preview (1).png"))); // NOI18N
        login.add(IMUNA);
        IMUNA.setBounds(140, 100, 240, 40);

        cpfLogin.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        cpfLogin.setForeground(new java.awt.Color(255, 255, 255));
        cpfLogin.setToolTipText("CPF");
        cpfLogin.setBorder(null);
        cpfLogin.setOpaque(false);
        cpfLogin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cpfLoginFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                cpfLoginFocusLost(evt);
            }
        });
        cpfLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cpfLoginMouseExited(evt);
            }
        });
        cpfLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cpfLoginActionPerformed(evt);
            }
        });
        cpfLogin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cpfLoginKeyPressed(evt);
            }
        });
        login.add(cpfLogin);
        cpfLogin.setBounds(240, 350, 130, 32);

        senhaLogin.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        senhaLogin.setForeground(new java.awt.Color(255, 255, 255));
        senhaLogin.setToolTipText("SENHA");
        senhaLogin.setBorder(null);
        senhaLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        senhaLogin.setOpaque(false);
        senhaLogin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                senhaLoginFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                senhaLoginFocusLost(evt);
            }
        });
        senhaLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                senhaLoginActionPerformed(evt);
            }
        });
        senhaLogin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                senhaLoginKeyPressed(evt);
            }
        });
        login.add(senhaLogin);
        senhaLogin.setBounds(240, 390, 130, 32);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/cpf.png"))); // NOI18N
        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });
        login.add(jLabel2);
        jLabel2.setBounds(140, 350, 250, 30);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/senha.png"))); // NOI18N
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });
        login.add(jLabel3);
        jLabel3.setBounds(140, 390, 250, 30);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sobre.png"))); // NOI18N
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });
        login.add(jLabel4);
        jLabel4.setBounds(270, 430, 118, 30);

        btnEntrar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btnEntrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/entrar.png"))); // NOI18N
        btnEntrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEntrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEntrarMouseClicked(evt);
            }
        });
        login.add(btnEntrar);
        btnEntrar.setBounds(137, 430, 130, 30);

        jLabel37.setForeground(new java.awt.Color(153, 153, 153));
        jLabel37.setText("CPF: 000.000.000-00");
        login.add(jLabel37);
        jLabel37.setBounds(10, 500, 110, 14);

        jLabel38.setForeground(new java.awt.Color(153, 153, 153));
        jLabel38.setText("Senha: admin");
        login.add(jLabel38);
        jLabel38.setBounds(10, 510, 90, 14);

        jLabel34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/ufufinal.png"))); // NOI18N
        login.add(jLabel34);
        jLabel34.setBounds(0, 0, 520, 532);

        jTextField1.setText("jTextField1");
        login.add(jTextField1);
        jTextField1.setBounds(40, 180, 60, 20);

        grupo.add(login, "card3");

        dados_gerais.setLayout(null);

        jButton3.setBackground(new java.awt.Color(15, 142, 165));
        jButton3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Voltar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        dados_gerais.add(jButton3);
        jButton3.setBounds(400, 500, 90, 23);

        vacinasUsadas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Vacina", "Lote", "Quantidade"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(vacinasUsadas);

        dados_gerais.add(jScrollPane5);
        jScrollPane5.setBounds(10, 80, 269, 161);

        jPanel12.setBackground(new java.awt.Color(153, 153, 153));

        numAgentesCadastrados.setFont(new java.awt.Font("Tahoma", 1, 80)); // NOI18N
        numAgentesCadastrados.setForeground(new java.awt.Color(255, 255, 255));
        numAgentesCadastrados.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        numAgentesCadastrados.setText("0");

        jLabel50.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel50.setText("AGENTES CADASTRADOS");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(numAgentesCadastrados, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(numAgentesCadastrados)
                .addGap(18, 18, 18)
                .addComponent(jLabel50)
                .addContainerGap())
        );

        dados_gerais.add(jPanel12);
        jPanel12.setBounds(290, 60, 206, 181);

        jPanel13.setBackground(new java.awt.Color(15, 142, 165));

        numTotalVacinados.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        numTotalVacinados.setForeground(new java.awt.Color(255, 255, 255));
        numTotalVacinados.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        numTotalVacinados.setText("0");

        jLabel51.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel51.setText("PESSOAS VACINADAS TOTAIS");

        numVacinadosSegundaDose.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        numVacinadosSegundaDose.setForeground(new java.awt.Color(255, 255, 255));
        numVacinadosSegundaDose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        numVacinadosSegundaDose.setText("0");

        jLabel53.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel53.setText("TAMBÉM COM A 2a DOSE");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addComponent(numTotalVacinados, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addComponent(numVacinadosSegundaDose, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addComponent(jLabel53, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(numTotalVacinados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel51)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numVacinadosSegundaDose)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel53)
                .addContainerGap())
        );

        dados_gerais.add(jPanel13);
        jPanel13.setBounds(290, 250, 206, 161);

        vacinadosPorIdade.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "10", "30", "50", "70", "90+"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(vacinadosPorIdade);

        dados_gerais.add(jScrollPane6);
        jScrollPane6.setBounds(10, 270, 269, 50);

        jPanel14.setBackground(new java.awt.Color(15, 142, 165));

        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("Vacinados por faixa etária:");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(100, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
        );

        dados_gerais.add(jPanel14);
        jPanel14.setBounds(10, 250, 280, 20);

        jPanel15.setBackground(new java.awt.Color(153, 153, 153));

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("Pessoas vacinadas por fabricante e lote:");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
        );

        dados_gerais.add(jPanel15);
        jPanel15.setBounds(10, 60, 320, 20);

        jPanel16.setBackground(new java.awt.Color(153, 153, 153));
        jPanel16.setLayout(null);

        totalVacinas.setFont(new java.awt.Font("Tahoma", 1, 44)); // NOI18N
        totalVacinas.setForeground(new java.awt.Color(255, 255, 255));
        totalVacinas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalVacinas.setText("0");
        jPanel16.add(totalVacinas);
        totalVacinas.setBounds(10, 10, 490, 43);

        jLabel49.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel49.setText("VACINAS TOTAIS CADASTRADAS NO SISTEMA");
        jPanel16.add(jLabel49);
        jLabel49.setBounds(10, 50, 490, 14);

        dados_gerais.add(jPanel16);
        jPanel16.setBounds(0, 420, 510, 70);

        jPanel17.setBackground(new java.awt.Color(15, 142, 165));
        jPanel17.setLayout(null);

        vacinasEmEstoque.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        vacinasEmEstoque.setForeground(new java.awt.Color(255, 255, 255));
        vacinasEmEstoque.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vacinasEmEstoque.setText("0");
        jPanel17.add(vacinasEmEstoque);
        vacinasEmEstoque.setBounds(10, 11, 230, 37);

        jLabel52.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setText("VACINAS DISPONÍVEIS");
        jPanel17.add(jLabel52);
        jLabel52.setBounds(0, 50, 250, 22);

        dados_gerais.add(jPanel17);
        jPanel17.setBounds(20, 330, 250, 70);

        jPanel18.setBackground(new java.awt.Color(0, 124, 146));

        jLabel48.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel48.setText("DADOS GERAIS");

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addComponent(jLabel48, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel48, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
        );

        dados_gerais.add(jPanel18);
        jPanel18.setBounds(0, -10, 510, 60);

        grupo.add(dados_gerais, "card8");

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Registrar Agente"));
        jPanel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel7MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel7MousePressed(evt);
            }
        });

        nomeAgente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nomeAgenteKeyPressed(evt);
            }
        });

        cpfAgente.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cpfAgenteFocusLost(evt);
            }
        });
        cpfAgente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cpfAgenteKeyPressed(evt);
            }
        });

        add_editar_agente.setText("Adicionar");
        add_editar_agente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add_editar_agente.setPreferredSize(new java.awt.Dimension(79, 20));
        add_editar_agente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_editar_agenteActionPerformed(evt);
            }
        });

        jLabel9.setText("Nome");

        jLabel13.setText("CPF");

        jLabel14.setText("Senha");

        permissao.setText("Admin");
        permissao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                permissaoActionPerformed(evt);
            }
        });

        limpar_excluir_agente.setText("Limpar");
        limpar_excluir_agente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        limpar_excluir_agente.setPreferredSize(new java.awt.Dimension(79, 20));
        limpar_excluir_agente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpar_excluir_agenteActionPerformed(evt);
            }
        });

        senhaAgente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                senhaAgenteActionPerformed(evt);
            }
        });
        senhaAgente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                senhaAgenteKeyPressed(evt);
            }
        });

        removerFoto.setText("Remover foto");
        removerFoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removerFotoActionPerformed(evt);
            }
        });

        sexoAgente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Masculino", "Feminino", "Outros" }));

        jLabel15.setText("Sexo");

        fotoPerfil.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fotoPerfil.setText("Selecione a foto");
        fotoPerfil.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        fotoPerfil.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        fotoPerfil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fotoPerfilMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(fotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13)
                    .addComponent(jLabel15)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nomeAgente)
                    .addComponent(cpfAgente)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(senhaAgente, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(permissao, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(sexoAgente, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(limpar_excluir_agente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(add_editar_agente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removerFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(limpar_excluir_agente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(add_editar_agente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cpfAgente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13)))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(nomeAgente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9))
                                .addGap(32, 32, 32)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(senhaAgente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(permissao, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sexoAgente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(removerFoto))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        agentes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome", "CPF", "Sexo", "Admin"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        agentes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                agentesMouseClicked(evt);
            }
        });
        agentes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                agentesKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                agentesKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(agentes);

        jButton7.setText("Voltar");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        pesquisarAgente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pesquisarAgenteMouseExited(evt);
            }
        });
        pesquisarAgente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pesquisarAgenteKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pesquisarAgenteKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                pesquisarAgenteKeyTyped(evt);
            }
        });

        jLabel20.setText("agentes cadastrados");

        numAgentes.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        numAgentes.setText("num");

        jLabel28.setText("Pesquisar:");

        javax.swing.GroupLayout registrarAgenteLayout = new javax.swing.GroupLayout(registrarAgente);
        registrarAgente.setLayout(registrarAgenteLayout);
        registrarAgenteLayout.setHorizontalGroup(
            registrarAgenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registrarAgenteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(registrarAgenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registrarAgenteLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pesquisarAgente, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(numAgentes, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20)
                        .addGap(21, 21, 21)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        registrarAgenteLayout.setVerticalGroup(
            registrarAgenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registrarAgenteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(registrarAgenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(pesquisarAgente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(numAgentes)
                    .addComponent(jLabel28))
                .addContainerGap())
        );

        grupo.add(registrarAgente, "card5");

        sobre.setLayout(null);

        jButton2.setBackground(new java.awt.Color(15, 142, 165));
        jButton2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Voltar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/IMUNA.png"))); // NOI18N

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        jTextArea1.setForeground(new java.awt.Color(51, 51, 51));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("IMUNA é um software desenvolvido para disciplina de Método e Técnicas de Programação — ministrado pela professora Eliana Pantaleão — utilizando Java e seu principal objetivo é servir como um auxiliador no controle de vacinação contra o COVID-19.\n\nDesenvolvido pelos alunos Matheus Lima, Italo Fernandes e Gabriel Martineli, do curso de Engenharia Eletrônica e de Telecomunicações da Universidade Federal de Uberlândia - Campus Patos de Minas.\n\nVersão 1.0.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane7.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane7)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sobre.add(jPanel1);
        jPanel1.setBounds(30, 40, 440, 450);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/dkdkskd.png"))); // NOI18N
        sobre.add(jLabel6);
        jLabel6.setBounds(0, -10, 540, 540);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/dkdkskd.png"))); // NOI18N
        sobre.add(jLabel1);
        jLabel1.setBounds(0, 0, 540, 540);

        grupo.add(sobre, "card2");

        registrarVacina.setLayout(null);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Registrar vacina"));
        jPanel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel8MouseClicked(evt);
            }
        });

        nomeVacina.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        loteVacina.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        loteVacina.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                loteVacinaFocusLost(evt);
            }
        });

        add_editar_vacina.setText("Adicionar");
        add_editar_vacina.setPreferredSize(new java.awt.Dimension(79, 20));
        add_editar_vacina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_editar_vacinaActionPerformed(evt);
            }
        });

        jLabel21.setText("Nome");

        jLabel22.setText("Lote");

        jLabel23.setText("Quantidade");

        jLabel24.setText("Intervalo");

        limpar_excluir_vacina.setText("Limpar");
        limpar_excluir_vacina.setPreferredSize(new java.awt.Dimension(79, 20));
        limpar_excluir_vacina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpar_excluir_vacinaActionPerformed(evt);
            }
        });

        quantidadeVacina.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        intervaloVacina.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loteVacina, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nomeVacina, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(44, 44, 44)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel24))
                .addGap(4, 4, 4)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(quantidadeVacina, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                    .addComponent(intervaloVacina))
                .addGap(32, 32, 32)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(add_editar_vacina, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(limpar_excluir_vacina, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(nomeVacina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel21))
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel23)
                                .addComponent(quantidadeVacina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(loteVacina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel22))
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel24)
                                .addComponent(intervaloVacina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(limpar_excluir_vacina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(add_editar_vacina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        registrarVacina.add(jPanel8);
        jPanel8.setBounds(10, 11, 485, 82);

        vacinas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome", "Lote", "Quantidade", "Data", "Intervalo", "Insersor"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        vacinas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vacinasMouseClicked(evt);
            }
        });
        vacinas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vacinasKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(vacinas);

        registrarVacina.add(jScrollPane2);
        jScrollPane2.setBounds(10, 99, 485, 369);

        jButton10.setText("Voltar");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        registrarVacina.add(jButton10);
        jButton10.setBounds(416, 505, 79, 23);

        pesquisaVacina.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
                pesquisaVacinaAncestorRemoved(evt);
            }
        });
        pesquisaVacina.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pesquisaVacinaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pesquisaVacinaKeyReleased(evt);
            }
        });
        registrarVacina.add(pesquisaVacina);
        pesquisaVacina.setBounds(117, 479, 156, 20);

        numVacinas.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        numVacinas.setText("0");
        registrarVacina.add(numVacinas);
        numVacinas.setBounds(307, 482, 72, 14);

        jLabel27.setText("vacina(s) cadastradas.");
        registrarVacina.add(jLabel27);
        jLabel27.setBounds(385, 482, 110, 14);

        intervalo1.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
                intervalo1AncestorMoved(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        intervalo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intervalo1ActionPerformed(evt);
            }
        });
        intervalo1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                intervalo1KeyPressed(evt);
            }
        });
        registrarVacina.add(intervalo1);
        intervalo1.setBounds(117, 509, 67, 20);

        intervalo2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                intervalo2FocusLost(evt);
            }
        });
        registrarVacina.add(intervalo2);
        intervalo2.setBounds(208, 509, 65, 20);

        jLabel54.setText("até");
        registrarVacina.add(jLabel54);
        jLabel54.setBounds(188, 512, 16, 14);

        porIntervaloVacina.setText("Por intervalo");
        porIntervaloVacina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                porIntervaloVacinaActionPerformed(evt);
            }
        });
        registrarVacina.add(porIntervaloVacina);
        porIntervaloVacina.setBounds(10, 508, 101, 23);

        jButton5.setText("Pesquisa geral");
        jButton5.setEnabled(false);
        registrarVacina.add(jButton5);
        jButton5.setBounds(10, 479, 101, 23);

        grupo.add(registrarVacina, "card6");

        jPanel2.setBackground(new java.awt.Color(0, 124, 146));
        jPanel2.setLayout(null);

        jButton6.setBackground(new java.awt.Color(244, 90, 51));
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Sair");
        jButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton6);
        jButton6.setBounds(420, 10, 73, 20);

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/Places-user-identity-icon.png"))); // NOI18N
        jLabel33.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel33MouseClicked(evt);
            }
        });
        jPanel2.add(jLabel33);
        jLabel33.setBounds(10, 0, 40, 40);

        jPanel6.setBackground(new java.awt.Color(15, 142, 165));

        jLabel7.setFont(new java.awt.Font("Cambria", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Bem vindo(a),");

        nome.setFont(new java.awt.Font("Cambria", 1, 12)); // NOI18N
        nome.setForeground(new java.awt.Color(255, 255, 255));
        nome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        nome.setText("fulano");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                .addComponent(nome))
        );

        jPanel2.add(jPanel6);
        jPanel6.setBounds(30, 10, 380, 20);

        dadosGerais.setBackground(new java.awt.Color(255, 255, 255));
        dadosGerais.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dadosGerais.setPreferredSize(new java.awt.Dimension(485, 128));
        dadosGerais.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dadosGeraisMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dadosGeraisMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dadosGeraisMouseExited(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/relatorio-de-saude.png"))); // NOI18N

        jLabel17.setFont(new java.awt.Font("Cambria", 1, 24)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Dados Gerais");

        jLabel29.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Consulte as estatísticas gerais da aplicação.");

        javax.swing.GroupLayout dadosGeraisLayout = new javax.swing.GroupLayout(dadosGerais);
        dadosGerais.setLayout(dadosGeraisLayout);
        dadosGeraisLayout.setHorizontalGroup(
            dadosGeraisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dadosGeraisLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel8)
                .addGap(32, 32, 32)
                .addGroup(dadosGeraisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        dadosGeraisLayout.setVerticalGroup(
            dadosGeraisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dadosGeraisLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel29)
                .addContainerGap(34, Short.MAX_VALUE))
            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        controleVacinacao.setBackground(new java.awt.Color(255, 255, 255));
        controleVacinacao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        controleVacinacao.setPreferredSize(new java.awt.Dimension(485, 128));
        controleVacinacao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                controleVacinacaoMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                controleVacinacaoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                controleVacinacaoMouseExited(evt);
            }
        });

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/vacina (2).png"))); // NOI18N

        jLabel16.setFont(new java.awt.Font("Cambria", 1, 24)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Controle da Vacinação");

        jLabel30.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setText("Insira, edite e exclua registros de vacinação.");

        javax.swing.GroupLayout controleVacinacaoLayout = new javax.swing.GroupLayout(controleVacinacao);
        controleVacinacao.setLayout(controleVacinacaoLayout);
        controleVacinacaoLayout.setHorizontalGroup(
            controleVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controleVacinacaoLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(controleVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        controleVacinacaoLayout.setVerticalGroup(
            controleVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controleVacinacaoLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel30)
                .addContainerGap(34, Short.MAX_VALUE))
            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        registroAgente.setBackground(new java.awt.Color(255, 255, 255));
        registroAgente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        registroAgente.setPreferredSize(new java.awt.Dimension(343, 100));
        registroAgente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                registroAgenteMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registroAgenteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registroAgenteMouseExited(evt);
            }
        });

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/enfermeira.png"))); // NOI18N

        jLabel18.setFont(new java.awt.Font("Cambria", 1, 24)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Registro de Agente");

        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setText("Consulte, edite e exclua agentes que possuem acesso ao sistema.");

        javax.swing.GroupLayout registroAgenteLayout = new javax.swing.GroupLayout(registroAgente);
        registroAgente.setLayout(registroAgenteLayout);
        registroAgenteLayout.setHorizontalGroup(
            registroAgenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registroAgenteLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel11)
                .addGap(30, 30, 30)
                .addGroup(registroAgenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 352, Short.MAX_VALUE))
                .addContainerGap())
        );
        registroAgenteLayout.setVerticalGroup(
            registroAgenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registroAgenteLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel31)
                .addContainerGap(34, Short.MAX_VALUE))
            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        registroVacina.setBackground(new java.awt.Color(255, 255, 255));
        registroVacina.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        registroVacina.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                registroVacinaMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registroVacinaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registroVacinaMouseExited(evt);
            }
        });

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/vacina (3).png"))); // NOI18N

        jLabel19.setFont(new java.awt.Font("Cambria", 1, 24)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Registro de Vacina");

        jLabel32.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel32.setText("Consulte, edite e exclua vacinas presentes no sistema.");

        javax.swing.GroupLayout registroVacinaLayout = new javax.swing.GroupLayout(registroVacina);
        registroVacina.setLayout(registroVacinaLayout);
        registroVacinaLayout.setHorizontalGroup(
            registroVacinaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registroVacinaLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel12)
                .addGap(53, 53, 53)
                .addGroup(registroVacinaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        registroVacinaLayout.setVerticalGroup(
            registroVacinaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registroVacinaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel32)
                .addContainerGap(35, Short.MAX_VALUE))
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controleVacinacao, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addComponent(dadosGerais, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .addComponent(registroVacina, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(registroAgente, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(dadosGerais, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controleVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(registroAgente, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(registroVacina, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout menuLayout = new javax.swing.GroupLayout(menu);
        menu.setLayout(menuLayout);
        menuLayout.setHorizontalGroup(
            menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 7, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        menuLayout.setVerticalGroup(
            menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        grupo.add(menu, "card4");

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Registrar vacinação"));

        jLabel5.setText("Nome");

        nomeVacinacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nomeVacinacaoActionPerformed(evt);
            }
        });

        cpfVacinacao.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cpfVacinacaoFocusLost(evt);
            }
        });

        jLabel25.setText("CPF");

        jLabel35.setText("Data de nascimento");

        vacinasDisponiveis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vacinasDisponiveisMouseClicked(evt);
            }
        });
        vacinasDisponiveis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vacinasDisponiveisActionPerformed(evt);
            }
        });

        jLabel39.setText("Vacina");

        add_editar_vacinacao.setText("Adicionar");
        add_editar_vacinacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_editar_vacinacaoActionPerformed(evt);
            }
        });

        limpar_excluir_vacinacao.setText("Limpar");
        limpar_excluir_vacinacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpar_excluir_vacinacaoActionPerformed(evt);
            }
        });

        jLabel42.setText("Tomou segunda dose?");

        segundaDoseVacinacao.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Não", "Sim" }));

        dataNascimentoVacinacao.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dataNascimentoVacinacaoFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel42)
                                .addGap(6, 6, 6)
                                .addComponent(segundaDoseVacinacao, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(nomeVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel39, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel35)
                        .addGap(18, 18, 18)
                        .addComponent(dataNascimentoVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(add_editar_vacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(limpar_excluir_vacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(vacinasDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cpfVacinacao, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(nomeVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vacinasDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cpfVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(jLabel42)
                    .addComponent(segundaDoseVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel35)
                        .addComponent(add_editar_vacinacao)
                        .addComponent(limpar_excluir_vacinacao))
                    .addComponent(dataNascimentoVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton1.setText("Voltar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        pesquisarVacinacao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pesquisarVacinacaoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pesquisarVacinacaoKeyReleased(evt);
            }
        });

        vacinacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome", "CPF", "Data de Nascimento", "Data", "Insersor", "Retorno", "Vacina", "2a dose"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        vacinacoes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vacinacoesMouseClicked(evt);
            }
        });
        vacinacoes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vacinacoesKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(vacinacoes);

        numVacinados.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        numVacinados.setText("0");

        jLabel41.setText("Pessoas vacinadas");

        porIntervaloVacinacao.setText("Por intervalo");
        porIntervaloVacinacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                porIntervaloVacinacaoActionPerformed(evt);
            }
        });

        intervalo1v.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
                intervalo1vAncestorMoved(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        intervalo1v.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intervalo1vActionPerformed(evt);
            }
        });
        intervalo1v.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                intervalo1vKeyPressed(evt);
            }
        });

        jLabel55.setText("até");

        intervalo2v.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                intervalo2vFocusLost(evt);
            }
        });

        jButton8.setText("Pesquisa geral");
        jButton8.setEnabled(false);

        javax.swing.GroupLayout registrarVacinacaoLayout = new javax.swing.GroupLayout(registrarVacinacao);
        registrarVacinacao.setLayout(registrarVacinacaoLayout);
        registrarVacinacaoLayout.setHorizontalGroup(
            registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registrarVacinacaoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registrarVacinacaoLayout.createSequentialGroup()
                        .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(porIntervaloVacinacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(6, 6, 6)
                        .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(registrarVacinacaoLayout.createSequentialGroup()
                                .addComponent(intervalo1v, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(jLabel55)
                                .addGap(4, 4, 4)
                                .addComponent(intervalo2v, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pesquisarVacinacao))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registrarVacinacaoLayout.createSequentialGroup()
                                .addComponent(numVacinados, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel41)))))
                .addContainerGap())
        );
        registrarVacinacaoLayout.setVerticalGroup(
            registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registrarVacinacaoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registrarVacinacaoLayout.createSequentialGroup()
                        .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel41)
                            .addComponent(numVacinados))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registrarVacinacaoLayout.createSequentialGroup()
                        .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton8)
                            .addComponent(pesquisarVacinacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(registrarVacinacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(porIntervaloVacinacao)
                            .addGroup(registrarVacinacaoLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(intervalo1v, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(registrarVacinacaoLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel55))
                            .addGroup(registrarVacinacaoLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(intervalo2v, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );

        grupo.add(registrarVacinacao, "card7");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(grupo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(grupo, javax.swing.GroupLayout.PREFERRED_SIZE, 532, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        setSize(new java.awt.Dimension(521, 571));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cpfLoginFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cpfLoginFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_cpfLoginFocusGained

    private void cpfLoginFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cpfLoginFocusLost

    }//GEN-LAST:event_cpfLoginFocusLost

    private void cpfLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cpfLoginActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cpfLoginActionPerformed

    private void senhaLoginFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_senhaLoginFocusGained

    }//GEN-LAST:event_senhaLoginFocusGained

    private void senhaLoginFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_senhaLoginFocusLost

    }//GEN-LAST:event_senhaLoginFocusLost

    private void senhaLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_senhaLoginActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_senhaLoginActionPerformed

    private void dadosGeraisMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dadosGeraisMouseEntered
        //mouse entra
        dadosGerais.setBackground(new java.awt.Color(0,154,187));
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
    }//GEN-LAST:event_dadosGeraisMouseEntered

    private void dadosGeraisMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dadosGeraisMouseExited
        //mouse sai
        dadosGerais.setBackground(new java.awt.Color(255,255,255));
        jLabel17.setForeground(new java.awt.Color(0, 0, 0));
    }//GEN-LAST:event_dadosGeraisMouseExited

    private void controleVacinacaoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_controleVacinacaoMouseEntered
        //mouse entra
        controleVacinacao.setBackground(new java.awt.Color(0,154,187));
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
    }//GEN-LAST:event_controleVacinacaoMouseEntered

    private void controleVacinacaoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_controleVacinacaoMouseExited
        //mouse sai
        controleVacinacao.setBackground(new java.awt.Color(255,255,255));
        jLabel16.setForeground(new java.awt.Color(0, 0, 0));
    }//GEN-LAST:event_controleVacinacaoMouseExited

    private void registroAgenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registroAgenteMouseEntered
        //mouse entra
        registroAgente.setBackground(new java.awt.Color(0,154,187));
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        
    }//GEN-LAST:event_registroAgenteMouseEntered

    private void registroAgenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registroAgenteMouseExited
        //mouse sai
        registroAgente.setBackground(new java.awt.Color(255,255,255));
        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
    }//GEN-LAST:event_registroAgenteMouseExited

    private void add_editar_agenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_editar_agenteActionPerformed
        String senha = String.valueOf(senhaAgente.getPassword());
        String sexo = String.valueOf(sexoAgente.getSelectedItem());
        
        if (add_editar_agente.getText().equals("Adicionar")) {//so funciona qnd o botao estiver como add
             if (senhaAgente.getPassword().equals("") || cpfAgente.getText().equals("   .   .   -  ") || nomeAgente.getText().equals("")) {//se todos os campos estiverem vazios
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            } else if (ger.compararCpf(cpfAgente.getText())) {
                try{
                ger.cadastrarAgente(nomeAgente.getText(), cpfAgente.getText(), sexo, permissao.isSelected(), senha, getImgBytes(imagem));
                }catch (Exception e){
                ger.cadastrarAgenteSemFoto(nomeAgente.getText(), cpfAgente.getText(), sexo, permissao.isSelected(), senha);
                }
                ger.atualizarTableAgentes(table);
            } else {//caso tiver, ele n adiciona
                JOptionPane.showMessageDialog(this, "CPF já cadastrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (add_editar_agente.getText().equals("Editar")) {
            int numLinha = 0;
            for (Agente agente : ger.getListaAgentes()) {
                if (agente.getCpf().equals(agentes.getValueAt(agentes.getSelectedRow(), 1))) {
                    break;
                }
                numLinha++;
            }
            if(agentes.getValueAt(agentes.getSelectedRow(), 1).equals("000.000.000-00")){
            JOptionPane.showMessageDialog(this, "Você não pode editar o Admin.");
        } else if (!ger.compararCpf(cpfAgente.getText()) && !cpfAgente.getText().equals(agentes.getValueAt(agenteSelectRow, 1))) {
                JOptionPane.showMessageDialog(this, "CPF já cadastrado.");
            } else if (senhaAgente.getPassword().equals("") || cpfAgente.getText().equals("   .   .   -  ") || nomeAgente.getText().equals("")) {//se todos os campos estiverem vazios
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            } else if (ger.deleteDBAgentes((String) agentes.getValueAt(agentes.getSelectedRow(), 1))) {
                try {
                    ger.editarAgente(numLinha, table, nomeAgente.getText(), cpfAgente.getText(), sexoAgente.getSelectedItem().toString(), permissao.isSelected(), senhaAgente.getText(), getImgBytes(imagem));
                } catch (Exception e) {
                    ger.editarAgenteSemFoto(numLinha, table, nomeAgente.getText(), cpfAgente.getText(), sexoAgente.getSelectedItem().toString(), permissao.isSelected(), senhaAgente.getText());
                }
                add_editar_agente.setText("Adicionar");
                limpar_excluir_agente.setText("Limpar");
                limparCamposAgente();
                pesquisarAgente.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Falha na comunicação com o banco de dados. Tente novamente.");
            }
        }
        
        imagem=null;
    }//GEN-LAST:event_add_editar_agenteActionPerformed

    private void senhaAgenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_senhaAgenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_senhaAgenteActionPerformed

    private void registroAgenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registroAgenteMouseClicked

        trocarAba(registrarAgente);
        
        ger.atualizarTableAgentes(table);
        
        numAgentes.setText(ger.getListaAgentes().size()+"");
        
        registroAgente.setBackground(new java.awt.Color(255,255,255));
        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
        
    }//GEN-LAST:event_registroAgenteMouseClicked

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        trocarAba(menu);
        limparCamposAgente();
               
        limpar_excluir_agente.setText("Limpar");
        add_editar_agente.setText("Adicionar");
        
        fotoPerfil.setIcon(null);
        
        removerFoto.setEnabled(false);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        trocarAba(login);
        cpfLogin.setText("");
        senhaLogin.setText("");
    }//GEN-LAST:event_jButton6ActionPerformed

    private void registroVacinaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registroVacinaMouseEntered
        // TODO add your handling code here:
        registroVacina.setBackground(new java.awt.Color(0,154,187));
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
    }//GEN-LAST:event_registroVacinaMouseEntered

    private void registroVacinaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registroVacinaMouseExited
        // TODO add your handling code here:
        registroVacina.setBackground(new java.awt.Color(255,255,255));
        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
    }//GEN-LAST:event_registroVacinaMouseExited

    private void cpfLoginMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cpfLoginMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_cpfLoginMouseExited

    private void limpar_excluir_agenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpar_excluir_agenteActionPerformed
        if (limpar_excluir_agente.getText().equals("Excluir")) {
            if (cpfLogin.getText().equals(agentes.getValueAt(agentes.getSelectedRow(), 1))){
                JOptionPane.showMessageDialog(this, "Você não pode excluir a si próprio.");
            } else if (!agentes.getValueAt(agentes.getSelectedRow(), 1).equals("000.000.000-00")) {
                if(ger.deleteDBAgentes((String) agentes.getValueAt(agentes.getSelectedRow(), 1))){
                    ger.excluirAgente(agentes.getSelectedRow(), table);
                    agentes.setModel(table);
                    numAgentes.setText(ger.getListaAgentes().size() + "");
                } else {
                    JOptionPane.showMessageDialog(this, "Falha na comunicação com o banco de dados. Tente novamente!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Você não pode excluir o Admin.");
            }
        }

        limparCamposAgente();
        limpar_excluir_agente.setText("Limpar");
        add_editar_agente.setText("Adicionar");
        pesquisarAgente.setEnabled(true);
    }//GEN-LAST:event_limpar_excluir_agenteActionPerformed

    private void agentesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_agentesMouseClicked
        imagem  = null;
        preencherCamposAgentes();
        pesquisarAgente.setEnabled(false);
        if(ger.consultarAgente(agentes.getValueAt(agentes.getSelectedRow(), 1)+"").getFotoPerfil() == null){
            removerFoto.setEnabled(false);
        } else {
            removerFoto.setEnabled(true);
        }
        limpar_excluir_agente.setText("Excluir");
        add_editar_agente.setText("Editar");
        
        agenteSelectRow = agentes.getSelectedRow();
    }//GEN-LAST:event_agentesMouseClicked

    private void jPanel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel7MouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jPanel7MouseClicked

    private void add_editar_vacinaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_editar_vacinaActionPerformed


        if(add_editar_vacina.getText().equals("Adicionar")){
            if(nomeVacina.getText().equals("")||
               loteVacina.getText().equals("")||
               quantidadeVacina.getText().equals("")||
               intervaloVacina.getText().equals("   dias")){
                
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                
            } else if(ger.compararLote(loteVacina.getText())){
                ger.cadastrarVacina(nomeVacina.getText(), loteVacina.getText(), getData(),  quantidadeVacina.getText(), intervaloVacina.getText(), ger.consultarAgente(cpfLogin.getText()).getNome());
                ger.atualizarTableVacinas(tableVacina);
            } else {
                JOptionPane.showMessageDialog(this, "Lote de vacina já cadastrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (add_editar_vacina.getText().equals("Editar")) {
            int numLinha = 0;
            for (Vacina vacina : ger.getListaVacinas()) {
                if (vacina.getLote().equals(vacinas.getValueAt(vacinas.getSelectedRow(), 1))) {
                    break;
                }
                numLinha++;
            }
            if(nomeVacina.getText().equals("")||
               loteVacina.getText().equals("")||
               quantidadeVacina.getText().equals("")||
               intervaloVacina.getText().equals("   dias")){
                
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            } else if(!ger.compararLote(loteVacina.getText())&&!loteVacina.getText().equals(vacinas.getValueAt(vacinaSelectRow, 1))){
            JOptionPane.showMessageDialog(this, "Lote de vacina já cadastrado.");
        } else if(ger.deleteDBVacinas(vacinas.getValueAt(vacinas.getSelectedRow(), 1)+"")){
                ger.editarVacina(tableVacina, nomeVacina.getText(), loteVacina.getText(), getData(), quantidadeVacina.getText(), intervaloVacina.getText(), ger.consultarAgente(cpfLogin.getText()).getNome(), numLinha);
                add_editar_vacina.setText("Adicionar");
                limpar_excluir_vacina.setText("Limpar");
                limparCamposVacina();
                pesquisaVacina.setEnabled(true);
                porIntervaloVacina.setEnabled(true);
            }else{
                JOptionPane.showMessageDialog(this, "Falha na comunicação com o banco de dados. Tente novamente!");
            }
        }
    }//GEN-LAST:event_add_editar_vacinaActionPerformed

    private void limpar_excluir_vacinaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpar_excluir_vacinaActionPerformed
        if (limpar_excluir_vacina.getText().equals("Excluir")) {
            if(ger.deleteDBVacinas(vacinas.getValueAt(vacinas.getSelectedRow(), 1)+"")){
                ger.excluirVacina(vacinas.getSelectedRow(), tableVacina);
                vacinas.setModel(tableVacina);
                numVacinas.setText(ger.getListaVacinas().size() + "");
                add_editar_vacina.setText("Adicionar");
                limpar_excluir_vacina.setText("Limpar");
                limparCamposVacina();
                pesquisaVacina.setEnabled(true);
                porIntervaloVacina.setEnabled(true);
            }else{
                JOptionPane.showMessageDialog(this, "Falha na comunicação com o banco de dados. Tente novamente!");
            }
        }
    }//GEN-LAST:event_limpar_excluir_vacinaActionPerformed

    private void jPanel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel8MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel8MouseClicked

    private void registroVacinaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registroVacinaMouseClicked
        trocarAba(registrarVacina);
        
        ger.atualizarTableVacinas(tableVacina);
        
        numVacinas.setText(ger.getListaVacinas().size()+"");
        
        registroVacina.setBackground(new java.awt.Color(255,255,255));
        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
    }//GEN-LAST:event_registroVacinaMouseClicked

    private void fecharWindow(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fecharWindow
        fecharJanela();
    }//GEN-LAST:event_fecharWindow

    private void abrirWindow(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_abrirWindow
        abrirJanela();
    }//GEN-LAST:event_abrirWindow

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        trocarAba(login);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        trocarAba(menu);
        limparCamposVacina();
        add_editar_vacina.setText("Adicionar");
        limpar_excluir_vacina.setText("Limpar");
    }//GEN-LAST:event_jButton10ActionPerformed

    private void vacinasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vacinasMouseClicked
        preencherCamposVacinas();
        add_editar_vacina.setText("Editar");
        limpar_excluir_vacina.setText("Excluir");
        vacinaSelectRow = vacinas.getSelectedRow();
        pesquisaVacina.setEnabled(false);
        porIntervaloVacina.setEnabled(false);
    }//GEN-LAST:event_vacinasMouseClicked

    private void pesquisarAgenteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisarAgenteKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_pesquisarAgenteKeyTyped

    private void pesquisarAgenteKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisarAgenteKeyPressed
        // TODO add your handling code here:
        ger.pesquisarAgente(pesquisarAgente.getText(), table);
    }//GEN-LAST:event_pesquisarAgenteKeyPressed

    private void pesquisarAgenteKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisarAgenteKeyReleased
        // TODO add your handling code here:
        ger.pesquisarAgente(pesquisarAgente.getText(), table);
    }//GEN-LAST:event_pesquisarAgenteKeyReleased

    private void loginMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginMousePressed

    }//GEN-LAST:event_loginMousePressed

    private void senhaLoginKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_senhaLoginKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            entrar();
        }
    }//GEN-LAST:event_senhaLoginKeyPressed

    private void cpfLoginKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cpfLoginKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            senhaLogin.grabFocus();
        }
    }//GEN-LAST:event_cpfLoginKeyPressed

    private void nomeAgenteKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomeAgenteKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            add_editar_agente.doClick();
        }
    }//GEN-LAST:event_nomeAgenteKeyPressed

    private void cpfAgenteKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cpfAgenteKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            add_editar_agente.doClick();
        }
    }//GEN-LAST:event_cpfAgenteKeyPressed

    private void senhaAgenteKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_senhaAgenteKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            add_editar_agente.doClick();
        }
    }//GEN-LAST:event_senhaAgenteKeyPressed

    private void pesquisarAgenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pesquisarAgenteMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_pesquisarAgenteMouseExited

    private void jLabel33MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel33MouseClicked
        // TODO add your handling code here:

        
    }//GEN-LAST:event_jLabel33MouseClicked

    private void btnEntrarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEntrarMouseClicked
        entrar();
    }//GEN-LAST:event_btnEntrarMouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        // TODO add your handling code here:
        trocarAba(sobre);
    }//GEN-LAST:event_jLabel4MouseClicked

    private void permissaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_permissaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_permissaoActionPerformed

    private void fotoPerfilMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fotoPerfilMouseClicked
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(null);

        if (res == JFileChooser.APPROVE_OPTION) {
            File arquivo = fc.getSelectedFile();
            imagem = setImagemDimensao(arquivo.getAbsolutePath(), 103, 103);
            fotoPerfil.setIcon(new ImageIcon(imagem));
        } else {
            JOptionPane.showMessageDialog(this, "Voce nao selecionou nenhum arquivo.");
        }
    }//GEN-LAST:event_fotoPerfilMouseClicked

    private void removerFotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removerFotoActionPerformed
        ger.getListaAgentes().get(agentes.getSelectedRow()).setFotoPerfil(null);
        ger.atualizarTableAgentes(table);
        ger.deleteDBAgentes((String) agentes.getValueAt(agenteSelectRow, 1));
        add_editar_agente.setText("Adicionar");
        limpar_excluir_agente.setText("Limpar");
        limparCamposAgente();
        pesquisarAgente.setEnabled(true);
        fotoPerfil.setIcon(null);
    }//GEN-LAST:event_removerFotoActionPerformed

    private void agentesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_agentesKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == KeyEvent.VK_ESCAPE){
        limparCamposAgente();
       
        limpar_excluir_agente.setText("Limpar");
        add_editar_agente.setText("Adicionar");
        
        fotoPerfil.setIcon(null);
        
        removerFoto.setEnabled(false);
        
        pesquisarAgente.setEnabled(true);
    }
    }//GEN-LAST:event_agentesKeyPressed

    private void agentesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_agentesKeyTyped
        // TODO add your handling code here:
        
    }//GEN-LAST:event_agentesKeyTyped

    private void jPanel7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel7MousePressed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jPanel7MousePressed

    private void vacinasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vacinasKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == KeyEvent.VK_ESCAPE){
        limparCamposVacina();
        add_editar_vacina.setText("Adicionar");
        limpar_excluir_vacina.setText("Limpar");
        pesquisaVacina.setEnabled(true);
        porIntervaloVacina.setEnabled(true);
        }
    }//GEN-LAST:event_vacinasKeyPressed

    private void controleVacinacaoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_controleVacinacaoMouseClicked
        trocarAba(registrarVacinacao);
        
        atualizarJComboBoxVacinasDisponiveis();
        
        numVacinados.setText(ger.getListaVacinacao().size()+"");
        
        controleVacinacao.setBackground(new java.awt.Color(255,255,255));
        jLabel16.setForeground(new java.awt.Color(0, 0, 0));
    }//GEN-LAST:event_controleVacinacaoMouseClicked

    private void dadosGeraisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dadosGeraisMouseClicked
        trocarAba(dados_gerais);
        atualizarTablePorFabricante();
        numAgentesCadastrados.setText(ger.getListaAgentes().size()+"");
        numTotalVacinados.setText(ger.getListaVacinacao().size()+"");
        vacinadosPorIdade();
        int tv = 0;
        for(Vacina vacina: ger.getListaVacinas()){
            tv = tv + Integer.parseInt(vacina.getQuantidade());
        }
        totalVacinas.setText(tv+"");
        vacinasEmEstoque.setText((tv-ger.getListaVacinacao().size())+"");
        
        dadosGerais.setBackground(new java.awt.Color(255,255,255));
        jLabel17.setForeground(new java.awt.Color(0, 0, 0));
        
        int segundaDose = 0;
        for(Vacinacao vacinacao: ger.getListaVacinacao()){
            if(vacinacao.getSegundaDose().equals("Sim")){
                segundaDose++;
            }
        }
        numVacinadosSegundaDose.setText(segundaDose+"");
    }//GEN-LAST:event_dadosGeraisMouseClicked

    private void nomeVacinacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nomeVacinacaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nomeVacinacaoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        trocarAba(menu);
        limparCamposVacinacao();
        limpar_excluir_vacinacao.setText("Limpar");
        add_editar_vacinacao.setText("Adicionar");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        trocarAba(menu);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        cpfLogin.grabFocus();
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        // TODO add your handling code here:
        senhaLogin.grabFocus();
    }//GEN-LAST:event_jLabel3MouseClicked

    private void vacinasDisponiveisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vacinasDisponiveisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vacinasDisponiveisActionPerformed

    private void add_editar_vacinacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_editar_vacinacaoActionPerformed
        if (add_editar_vacinacao.getText().equals("Adicionar")) {
            if (nomeVacinacao.getText().equals("") || dataNascimentoVacinacao.getDate() == null || cpfVacinacao.getText().equals("   .   .   -  ")) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos. Verifique também se a data é válida pela cor: verde = válida; vermelha = inválida.");
            } else if (Integer.parseInt(getIdade(dateToStringFormatado(dataNascimentoVacinacao.getDate())))<10){
                JOptionPane.showMessageDialog(this, "A pessoa que você tentou cadastrar a vacinação tem "+getIdade(dateToStringFormatado(dataNascimentoVacinacao.getDate()))+" anos, inferior a idade mínima de 10 anos.");
            } else if (Integer.parseInt(getIdade(dateToStringFormatado(dataNascimentoVacinacao.getDate())))>122){
                JOptionPane.showMessageDialog(this, "A pessoa que você tentou cadastrar a vacinação tem "+getIdade(dateToStringFormatado(dataNascimentoVacinacao.getDate()))+" anos, superior a idade máxima de 122 anos.");
            } else if (ger.compararCPFVacinado(cpfVacinacao.getText())) {
                ger.cadastrarVacinacao(nomeVacinacao.getText(), cpfVacinacao.getText(), dateToStringFormatado(dataNascimentoVacinacao.getDate()), getData(), ger.consultarAgente(cpfLogin.getText()).getNome(), getRetorno(), vacinasDisponiveis.getSelectedItem().toString(), segundaDoseVacinacao.getSelectedItem().toString());
                ger.atualizarTableVacinacao(tableVacinacao);
                limparCamposVacinacao();          
            } else {
                JOptionPane.showMessageDialog(this, "Pessoa já vacinada com pelo menos uma dose.");
            }
        }
        
        if (add_editar_vacinacao.getText().equals("Editar")) {
            int numLinha = 0;
            for (Vacinacao vacinacao : ger.getListaVacinacao()) {
                if (vacinacao.getCpf().equals(vacinacoes.getValueAt(vacinacoes.getSelectedRow(), 1))) {
                    break;
                }
                numLinha++;
            }
            if (nomeVacinacao.getText().equals("") || dataNascimentoVacinacao.getDate() == null || cpfVacinacao.getText().equals("   .   .   -  ")) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos. Verifique também se a data é válida pela cor: verde = válida; vermelha = inválida.");
            } else if(!ger.compararCPFVacinado(cpfVacinacao.getText())&&!cpfVacinacao.getText().equals(vacinacoes.getValueAt(vacinacaoSelectRow, 1))){
            JOptionPane.showMessageDialog(this, "Esse CPF é de uma pessoa já vacinada.");
        } else if (ger.deleteDBVacinacao(vacinacoes.getValueAt(vacinacoes.getSelectedRow(), 1) + "")) {
                ger.editarVacinacao(tableVacinacao, nomeVacinacao.getText(), cpfVacinacao.getText(), dateToStringFormatado(dataNascimentoVacinacao.getDate()), getData(), ger.consultarAgente(cpfLogin.getText()).getNome(), getRetorno(), vacinasDisponiveis.getSelectedItem().toString(), segundaDoseVacinacao.getSelectedItem().toString(), numLinha);
                limparCamposVacinacao();
                add_editar_vacinacao.setText("Adicionar");
                limpar_excluir_vacinacao.setText("Limpar");
                pesquisarVacinacao.setEnabled(true);
                porIntervaloVacinacao.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Falha na comunicação com o banco de dados. Tente novamente!");
            }
        }
    }//GEN-LAST:event_add_editar_vacinacaoActionPerformed

    private void pesquisarVacinacaoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisarVacinacaoKeyPressed
        // TODO add your handling code here:
        ger.pesquisarVacinacao(pesquisarVacinacao.getText(), tableVacinacao);
    }//GEN-LAST:event_pesquisarVacinacaoKeyPressed

    private void pesquisaVacinaAncestorRemoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_pesquisaVacinaAncestorRemoved
        // TODO add your handling code here:
    }//GEN-LAST:event_pesquisaVacinaAncestorRemoved

    private void pesquisarVacinacaoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisarVacinacaoKeyReleased
        // TODO add your handling code here:
        ger.pesquisarVacinacao(pesquisarVacinacao.getText(), tableVacinacao);
    }//GEN-LAST:event_pesquisarVacinacaoKeyReleased

    private void limpar_excluir_vacinacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpar_excluir_vacinacaoActionPerformed
        if(limpar_excluir_vacinacao.getText().equals("Excluir")){
            if(ger.deleteDBVacinacao(vacinacoes.getValueAt(vacinacoes.getSelectedRow(), 1)+"")){
            ger.excluirVacinacao(vacinacoes.getSelectedRow(), tableVacinacao);
            vacinacoes.setModel(tableVacinacao);
            limpar_excluir_vacinacao.setText("Limpar");
            add_editar_vacinacao.setText("Adicionar");
            limparCamposVacinacao();
            pesquisarVacinacao.setEnabled(true);
            porIntervaloVacinacao.setEnabled(true);
            }else{
                JOptionPane.showMessageDialog(this, "Falha de comunicação com o banco de dados. Tente novamente!");
            }
        }
    }//GEN-LAST:event_limpar_excluir_vacinacaoActionPerformed

    private void vacinacoesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vacinacoesMouseClicked
        preencherCamposVacinacao();
        add_editar_vacinacao.setText("Editar");
        limpar_excluir_vacinacao.setText("Excluir");
        vacinacaoSelectRow = vacinacoes.getSelectedRow();
        pesquisarVacinacao.setEnabled(false);
        porIntervaloVacinacao.setEnabled(false);
    }//GEN-LAST:event_vacinacoesMouseClicked

    private void vacinacoesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vacinacoesKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == KeyEvent.VK_ESCAPE){
            limparCamposVacinacao();
            add_editar_vacinacao.setText("Adicionar");
            limpar_excluir_vacinacao.setText("Limpar");
            pesquisarVacinacao.setEnabled(true);
            porIntervaloVacinacao.setEnabled(true);
        }
    }//GEN-LAST:event_vacinacoesKeyPressed

    private void pesquisaVacinaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisaVacinaKeyPressed
        // TODO add your handling code here:
        ger.pesquisarVacina(pesquisaVacina.getText(), tableVacina);
    }//GEN-LAST:event_pesquisaVacinaKeyPressed

    private void pesquisaVacinaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisaVacinaKeyReleased
        // TODO add your handling code here:
        ger.pesquisarVacina(pesquisaVacina.getText(), tableVacina);
    }//GEN-LAST:event_pesquisaVacinaKeyReleased

    private void vacinasDisponiveisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vacinasDisponiveisMouseClicked
        // TODO add your handling code here:
        atualizarJComboBoxVacinasDisponiveis();
    }//GEN-LAST:event_vacinasDisponiveisMouseClicked

    private void loteVacinaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loteVacinaFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_loteVacinaFocusLost

    private void cpfAgenteFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cpfAgenteFocusLost

    }//GEN-LAST:event_cpfAgenteFocusLost

    private void cpfVacinacaoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cpfVacinacaoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_cpfVacinacaoFocusLost

    private void intervalo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intervalo1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_intervalo1ActionPerformed

    private void intervalo1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_intervalo1KeyPressed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_intervalo1KeyPressed

    private void intervalo2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_intervalo2FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_intervalo2FocusLost

    private void porIntervaloVacinaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_porIntervaloVacinaActionPerformed
        // TODO add your handling code here:
        if(ger.pesquisarVacinaPorIntervalo(intervalo1.getText(), intervalo2.getText(), tableVacina))
            JOptionPane.showMessageDialog(this, "Data inválida.");
        intervalo1.setText("");
        intervalo2.setText("");
    }//GEN-LAST:event_porIntervaloVacinaActionPerformed

    private void intervalo1AncestorMoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_intervalo1AncestorMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_intervalo1AncestorMoved

    private void dataNascimentoVacinacaoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataNascimentoVacinacaoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_dataNascimentoVacinacaoFocusLost

    private void porIntervaloVacinacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_porIntervaloVacinacaoActionPerformed
        // TODO add your handling code here:
        if(ger.pesquisarVacinacaoPorIntervalo(intervalo1v.getText(), intervalo2v.getText(), tableVacinacao))
            JOptionPane.showMessageDialog(this, "Data inválida.");
        intervalo1v.setText("");
        intervalo2v.setText("");
    }//GEN-LAST:event_porIntervaloVacinacaoActionPerformed

    private void intervalo1vAncestorMoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_intervalo1vAncestorMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_intervalo1vAncestorMoved

    private void intervalo1vActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intervalo1vActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_intervalo1vActionPerformed

    private void intervalo1vKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_intervalo1vKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_intervalo1vKeyPressed

    private void intervalo2vFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_intervalo2vFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_intervalo2vFocusLost

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
       
        
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows Classic".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            
        } catch (InstantiationException ex) {
            
        } catch (IllegalAccessException ex) {
            
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel IMUNA;
    private javax.swing.JButton add_editar_agente;
    private javax.swing.JButton add_editar_vacina;
    private javax.swing.JButton add_editar_vacinacao;
    private javax.swing.JTable agentes;
    private javax.swing.JLabel btnEntrar;
    private javax.swing.JPanel carregando;
    private javax.swing.JPanel controleVacinacao;
    private javax.swing.JTextField cpfAgente;
    private javax.swing.JTextField cpfLogin;
    private javax.swing.JTextField cpfVacinacao;
    private javax.swing.JPanel dadosGerais;
    private javax.swing.JPanel dados_gerais;
    private com.toedter.calendar.JDateChooser dataNascimentoVacinacao;
    private javax.swing.JLabel fotoPerfil;
    private javax.swing.JPanel grupo;
    private javax.swing.JTextField intervalo1;
    private javax.swing.JTextField intervalo1v;
    private javax.swing.JTextField intervalo2;
    private javax.swing.JTextField intervalo2v;
    private javax.swing.JTextField intervaloVacina;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton limpar_excluir_agente;
    private javax.swing.JButton limpar_excluir_vacina;
    private javax.swing.JButton limpar_excluir_vacinacao;
    private javax.swing.JPanel login;
    private javax.swing.JLabel logo;
    private javax.swing.JTextField loteVacina;
    private javax.swing.JPanel menu;
    private javax.swing.JLabel nome;
    private javax.swing.JTextField nomeAgente;
    private javax.swing.JTextField nomeVacina;
    private javax.swing.JTextField nomeVacinacao;
    private javax.swing.JLabel numAgentes;
    private javax.swing.JLabel numAgentesCadastrados;
    private javax.swing.JLabel numTotalVacinados;
    private javax.swing.JLabel numVacinados;
    private javax.swing.JLabel numVacinadosSegundaDose;
    private javax.swing.JLabel numVacinas;
    private javax.swing.JCheckBox permissao;
    private javax.swing.JTextField pesquisaVacina;
    private javax.swing.JTextField pesquisarAgente;
    private javax.swing.JTextField pesquisarVacinacao;
    private javax.swing.JButton porIntervaloVacina;
    private javax.swing.JButton porIntervaloVacinacao;
    private javax.swing.JTextField quantidadeVacina;
    private javax.swing.JPanel registrarAgente;
    private javax.swing.JPanel registrarVacina;
    private javax.swing.JPanel registrarVacinacao;
    private javax.swing.JPanel registroAgente;
    private javax.swing.JPanel registroVacina;
    private javax.swing.JButton removerFoto;
    private javax.swing.JComboBox<String> segundaDoseVacinacao;
    private javax.swing.JPasswordField senhaAgente;
    private javax.swing.JPasswordField senhaLogin;
    private javax.swing.JComboBox<String> sexoAgente;
    private javax.swing.JPanel sobre;
    private javax.swing.JLabel totalVacinas;
    private javax.swing.JTable vacinacoes;
    private javax.swing.JTable vacinadosPorIdade;
    private javax.swing.JTable vacinas;
    private javax.swing.JComboBox<String> vacinasDisponiveis;
    private javax.swing.JLabel vacinasEmEstoque;
    private javax.swing.JTable vacinasUsadas;
    // End of variables declaration//GEN-END:variables
}
