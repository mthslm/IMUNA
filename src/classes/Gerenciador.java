package classes;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;

/**Classe para controlar todo o programa, contendo todos os métodos necessários para gerenciamento
 * @version 1.0
 * @author Matheus Lima
 */

public class Gerenciador {
    private ArrayList <Agente> listaAgentes = new ArrayList();
    private ArrayList <Vacina> listaVacinas = new ArrayList();
    private ArrayList <Vacinacao> listaVacinacao = new ArrayList();
    private ArrayList <Agente> listaAgentesBefore = new ArrayList();
    private Agente agente;
    private Vacina vacina;
    private Vacinacao vacinacao;
    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    
    /** Método cadastra um novo agente na ArrayList listaAgentes
     * @param nome
     * @param cpf
     * @param sexo
     * @param permissao
     * @param senha 
     * @param fotoPerfil 
     */
    public void cadastrarAgente(String nome, String cpf, String sexo, boolean permissao, String senha, byte[] fotoPerfil){
        agente = new Agente(nome, cpf, sexo, permissao, senha, fotoPerfil);
        listaAgentes.add(agente);
    }
    
    /**
     * Cadastrar agente sem foto, mesma coisa do método anterior, mas sem foto.
     * @param nome
     * @param cpf
     * @param sexo
     * @param permissao
     * @param senha 
     */
    public void cadastrarAgenteSemFoto(String nome, String cpf, String sexo, boolean permissao, String senha){
        agente = new Agente(nome, cpf, sexo, permissao, senha, null);
        listaAgentes.add(agente);
    }
    
    /** Método que compara o CPF do novo agente com todos os outros cadastrados na ArrayList listaAgentes para dar permissão de cadastrar. O novo CPF não pode ser igual a de nenhum outro já criado, pois além de cada pessoa ter este dado distinto, ele é a forma de login
     * @param cpf
     * @return false, se houver outro agente com o mesmo CPF ou true, se não
     */
    public boolean compararCpf(String cpf){
        for(Agente ag: listaAgentes){
            if(cpf.equals(ag.getCpf())){
                return false;
            }
        }
        return true;
    }
    
    /** Método que consulta qual agente está logado no momento em que ele é chamado
     * @param cpf
     * @return Agente
     */
    public Agente consultarAgente(String cpf){
        for(Agente ag: listaAgentes){
            if(cpf.equals(ag.getCpf())){
                return ag;
            }
        }
        return null;
    }
    
    /** Método para excluir um agente da ArrayList e da table e então atualizar a tabela de agentes
     * @param i
     * @param table 
     */
    public void excluirAgente(int i, DefaultTableModel table) {
        listaAgentes.remove(i);
        table.removeRow(i);
        atualizarTableAgentes(table);
    }
    
    /** Método para editar um agente no ArrayList e então atualizar a tabela com base nisso
     * @param numLinha
     * @param table
     * @param nome
     * @param cpf
     * @param sexo
     * @param permissao
     * @param senha 
     */
    public void editarAgente(int numLinha, DefaultTableModel table, String nome, String cpf, String sexo, boolean permissao, String senha, byte[] fotoPerfil){
        table.setRowCount(0);//limpar a table
        listaAgentes.get(numLinha).setNome(nome);
        listaAgentes.get(numLinha).setCpf(cpf);
        listaAgentes.get(numLinha).setPermissao(permissao);
        listaAgentes.get(numLinha).setSenha(senha);
        listaAgentes.get(numLinha).setSexo(sexo);
        listaAgentes.get(numLinha).setFotoPerfil(fotoPerfil);
        atualizarTableAgentes(table);
    }
    
    /**
     * Método para editar um agente sem foto no ArrayList e então atualizar a tabela com base nisso
     * @param numLinha
     * @param table
     * @param nome
     * @param cpf
     * @param sexo
     * @param permissao
     * @param senha 
     */
    public void editarAgenteSemFoto(int numLinha, DefaultTableModel table, String nome, String cpf, String sexo, boolean permissao, String senha){
        table.setRowCount(0);//limpar a table
        listaAgentes.get(numLinha).setNome(nome);
        listaAgentes.get(numLinha).setCpf(cpf);
        listaAgentes.get(numLinha).setPermissao(permissao);
        listaAgentes.get(numLinha).setSenha(senha);
        listaAgentes.get(numLinha).setSexo(sexo);
        atualizarTableAgentes(table);
    }

    public ArrayList<Agente> getListaAgentes() {
        return listaAgentes;
    }

    public void setListaAgentes(ArrayList<Agente> listaAgentes) {
        this.listaAgentes = listaAgentes;
    }
    
    /** Método que verifica se há algum item do ArrayList que possui o mesmo CPF e senha dos parâmetros
     * @param cpf
     * @param senha
     * @return true, se existir ou false, se não
     */
    public boolean login(String cpf, String senha){
        for(Agente m: listaAgentes){
            if(cpf.equals(m.getCpf())&&senha.equals(m.getSenha())){
                return true;
            }
        }
        return false;
    }
    
    /** Método que atualiza a tabela de agentes de acordo com a ArrayList listaAgentes atualizada
     * @param table 
     */
    public void atualizarTableAgentes(DefaultTableModel table){
        table.setRowCount(0);
        for(int i=0;i<listaAgentes.size();i++){
            table.addRow(new Object[] {listaAgentes.get(i).getNome(),listaAgentes.get(i).getCpf(),listaAgentes.get(i).getSexo(),listaAgentes.get(i).getPermissao()});
        }
    }
    
    /** Método que pesquisa de acordo com cpf, nome e sexo os agentes contidos na listaAgentes e atualiza a tabela de acordo
     * @param pesquisa
     * @param table 
     */
    public void pesquisarAgente(String pesquisa,DefaultTableModel table){
        table.setRowCount(0);
        pesquisa = pesquisa.toLowerCase();
        for(Agente ag: listaAgentes){
            if(ag.getCpf().contains(pesquisa)||ag.getNome().toLowerCase().contains(pesquisa)||ag.getSexo().toLowerCase().contains(pesquisa)){
                table.addRow(new Object[] {ag.getNome(),ag.getCpf(),ag.getSexo(),ag.getPermissao()});
            } else if(pesquisa.equals("")){
                atualizarTableAgentes(table);
            }
        }
    }
    
    /**
     * Método que insere no banco de dados os agentes contidos na ArrayList listaAgentes
     * @return true or false
     */
    public boolean exportarDBAgentes() {
        conexao = Conexao.conector();
        String sql = "INSERT INTO tb_agentes (nome,cpf,sexo,permissao,senha,fotoPerfil) SELECT * FROM (SELECT ? AS nome, ? AS cpf, ? AS sexo, ? AS permissao, ? AS senha, ? AS fotoPerfil) AS tmp WHERE NOT EXISTS (SELECT cpf FROM tb_agentes WHERE cpf = ?) LIMIT 1";
        try {
            pst = conexao.prepareStatement(sql);
            for (Agente m: listaAgentes) {
                pst.setString(1, m.getNome());
                pst.setString(2, m.getCpf());
                pst.setString(3, m.getSexo());
                pst.setString(4, String.valueOf(m.getPermissao()));
                pst.setString(5, m.getSenha());
                pst.setBytes(6, m.getFotoPerfil());
                pst.setString(7, m.getCpf());
                //atualiza a tabela da db com os dados da arraylist
                pst.executeUpdate();
            }
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Método que deleta um agente da db
     * @return true, se o try{} foi bem sucedido, ou seja, ele conseguiu deletar toda a table tb_agentes da DB e fechar a conexão e false, se não
     */
    public boolean deleteDBAgentes(String cpf){
        conexao = Conexao.conector();
        String sql = "delete from tb_agentes where cpf = ?";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, cpf);
            pst.executeUpdate();
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Método importa do banco de dados para a ArrayList listaAgentes
     * @return true, se a importação for efetuada com sucesso e false, se não
     */
    public boolean importarDBAgentes() {
        conexao = Conexao.conector();
        String sql = "select * from tb_agentes";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            while(rs.next()){
                listaAgentes.add(new Agente(rs.getString(1),rs.getString(2),rs.getString(3),Boolean.parseBoolean(rs.getString(4)),rs.getString(5),rs.getBytes(6)));
            }
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
           
/////////////////////////////////vacina///////////////////////////////////////////////////

    
    /**
     * Cadastra nova vacina na ArrayList listaVacinas
     * @param nome
     * @param lote
     * @param data
     * @param quantidade
     * @param intervalo
     * @param insersor 
     */
    public void cadastrarVacina(String nome, String lote, String data, String quantidade, String intervalo, String insersor){
        vacina = new Vacina(nome, lote, data, quantidade, intervalo, insersor);
        listaVacinas.add(vacina);
    }
    
    /**
     * Atualiza a tabela de vacinas de acordo com a ArrayList listaVacinas
     * @param tableVacina 
     */
    public void atualizarTableVacinas(DefaultTableModel tableVacina){
        tableVacina.setRowCount(0);
        for(Vacina vac: listaVacinas){
            tableVacina.addRow(new Object[] {vac.getNome(),vac.getLote(),vac.getQuantidade(),vac.getData(),vac.getIntervalo(),vac.getInsersor()});
        }
    }

    public ArrayList<Vacina> getListaVacinas() {
        return listaVacinas;
    }

    /**
     * Método que pesquisa de acordo com cpf, nome e sexo os agentes contidos na listaAgentes e atualiza a tabela de acordo 
     * @param pesquisa
     * @param tableVacina 
     */
    public void pesquisarVacina(String pesquisa,DefaultTableModel tableVacina){
        tableVacina.setRowCount(0);
        pesquisa = pesquisa.toLowerCase();
        for(Vacina vac: listaVacinas) {
            if(vac.getNome().toLowerCase().contains(pesquisa)||
               vac.getLote().toLowerCase().contains(pesquisa)||
               vac.getQuantidade().contains(pesquisa)||
               vac.getData().contains(pesquisa)||
               vac.getIntervalo().toLowerCase().contains(pesquisa)) {
                tableVacina.addRow(new Object[] {vac.getNome(),vac.getLote(),vac.getQuantidade(),vac.getData(),vac.getIntervalo(),vac.getInsersor()});
            } else if(pesquisa.equals("")){
                atualizarTableAgentes(tableVacina);
            }
        }
    }
    
    /**
     * Método que pesquisa os registros por intervalo de datas
     * @param intervalo1
     * @param intervalo2
     * @param tableVacina
     * @return false or true
     */
    public boolean pesquisarVacinaPorIntervalo(String intervalo1, String intervalo2, DefaultTableModel tableVacina) {
        try {
            java.util.Date data1 = new SimpleDateFormat("MM/dd/yyyy").parse(intervalo1);
            java.util.Date data3 = new SimpleDateFormat("MM/dd/yyyy").parse(intervalo2);
            tableVacina.setRowCount(0);
            for(Vacina vac: listaVacinas){
                java.util.Date data2 = new SimpleDateFormat("MM/dd/yyyy").parse(vac.getData());
                if((data1.before(data2)||data1.equals(data2))&&(data2.before(data3)||data2.equals(data3))){
                    tableVacina.addRow(new Object[] {vac.getNome(),vac.getLote(),vac.getQuantidade(),vac.getData(),vac.getIntervalo(),vac.getInsersor()});
                }
            }
            return false;
        } catch (ParseException ex) {
            return true;
        }
    }
    
    
    /**
     * Compara se o lote do parâmetro é igual a um dos lotes já cadastrados na ArrayList listaVacinas
     * @param nome
     * @return false, se houver outro lote com o mesmo nome ou true se não
     */
    public boolean compararLote(String nome){
        for(Vacina vac: listaVacinas){
            if(nome.equals(vac.getLote())){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Método que edita uma vacina na ArrayList e em seguida chamada o método atualizaTableVacinas
     * @param tableVacina
     * @param nome
     * @param lote
     * @param data
     * @param quantidade
     * @param intervalo
     * @param insersor
     * @param numLinha 
     */
    public void editarVacina(DefaultTableModel tableVacina, String nome, String lote, String data, String quantidade, String intervalo, String insersor, int numLinha){
        tableVacina.setRowCount(0);
        listaVacinas.get(numLinha).setInsersor(insersor);
        listaVacinas.get(numLinha).setIntervalo(intervalo);
        listaVacinas.get(numLinha).setLote(lote);
        listaVacinas.get(numLinha).setNome(nome);
        listaVacinas.get(numLinha).setQuantidade(quantidade);
        listaVacinas.get(numLinha).setData(data);
        atualizarTableVacinas(tableVacina);
    }
    
    /**
     * Exclui vacina da ArrayList e da table e chama o método atualizarTableVacinas
     * @param i
     * @param table 
     */
    public void excluirVacina(int i, DefaultTableModel table) {
        listaVacinas.remove(i);
        table.removeRow(i);
        atualizarTableVacinas(table);
    }
    
    /**
     * Método que insere no banco de dados os agentes contidos na ArrayList listaVacinas
     * @return true or false
     */
    public boolean exportarDBVacinas() {
        conexao = Conexao.conector();
        String sql = "INSERT INTO tb_vacinas (nome,lote,dat,qtidade,intervalo,insersor) SELECT * FROM (SELECT ? AS nome, ? AS lote, ? AS dat, ? AS qtidade, ? AS intervalo, ? AS insersor) AS tmp WHERE NOT EXISTS (SELECT lote FROM tb_vacinas WHERE lote = ?) LIMIT 1";
        try {
            pst = conexao.prepareStatement(sql);
            for (Vacina m: listaVacinas) {
                pst.setString(1, m.getNome());
                pst.setString(2, m.getLote());
                String data = m.getData().substring(6)+"-"+m.getData().substring(3, 5)+"-"+m.getData().substring(0, 3);
                pst.setString(3, data);
                pst.setString(4, m.getQuantidade());
                pst.setString(5, m.getIntervalo());
                pst.setString(6, m.getInsersor());
                pst.setString(7, m.getLote());
                //atualiza a tabela da db com os dados da arraylist
                pst.executeUpdate();
            }
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Método que deleta uma vacina da db
     * @return true, se conseguir, false, se não
     */
    public boolean deleteDBVacinas(String lote){
        conexao = Conexao.conector();
        String sql = "delete from tb_vacinas where lote = ?";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, lote);
            pst.executeUpdate();
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Método importa as vacinas da db pro ArrayList listaVacinas
     * @return true, se conseguir, false, se não
     */
    public boolean importarDBVacinas() {
        conexao = Conexao.conector();
        String sql = "select * from tb_vacinas";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            while(rs.next()){
                String data = rs.getString(3).substring(8)+"/"+rs.getString(3).substring(5, 7)+"/"+rs.getString(3).substring(0,4);
                listaVacinas.add(new Vacina(rs.getString(1),rs.getString(2),data,rs.getString(4),rs.getString(5),rs.getString(6)));
            }
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    //--------------------------vacinacao----------------------------------//
    
    /**
     * Método para cadastrar nova vacinação e adicionar na ArrayList listaVacinacao
     * @param nome
     * @param cpf
     * @param dataNasc
     * @param data
     * @param insersor
     * @param retorno
     * @param vacinaLote
     * @param segundaDose 
     */
    public void cadastrarVacinacao(String nome, String cpf, String dataNasc, String data, String insersor, String retorno, String vacinaLote, String segundaDose){
        listaVacinacao.add(new Vacinacao(nome, cpf, dataNasc, data, insersor, retorno, vacinaLote, segundaDose));
    }
    
    /**
     * Método atualiza a tableVacinacao conforme a ArrayList listaVacinacao
     * @param tableVacinacao 
     */
    public void atualizarTableVacinacao(DefaultTableModel tableVacinacao){
        tableVacinacao.setRowCount(0);
        for(Vacinacao vacinacao: listaVacinacao){
            tableVacinacao.addRow(new Object[] {vacinacao.getNome(),vacinacao.getCpf(),vacinacao.getDataNasc(),vacinacao.getData(),vacinacao.getInsersor(),vacinacao.getRetorno(),vacinacao.getVacinaLote(),vacinacao.getSegundaDose()});
        }
    }

    public ArrayList<Vacinacao> getListaVacinacao() {
        return listaVacinacao;
    }
    
    /**
     * Método atualiza a tableVacinacao conforme o campo de pesquisa
     * @param pesquisa
     * @param tableVacinacao 
     */
    public void pesquisarVacinacao(String pesquisa,DefaultTableModel tableVacinacao){
        tableVacinacao.setRowCount(0);
        pesquisa = pesquisa.toLowerCase();//nao diferencia maiuscula de minuscula
        for(Vacinacao v: listaVacinacao) {
            if(v.getNome().toLowerCase().contains(pesquisa)||
               v.getCpf().contains(pesquisa)||
               v.getDataNasc().contains(pesquisa)||
               v.getData().contains(pesquisa)||
               v.getInsersor().toLowerCase().contains(pesquisa)||
               v.getRetorno().contains(pesquisa)||
               v.getVacinaLote().toLowerCase().contains(pesquisa)) {
                
                tableVacinacao.addRow(new Object[] {v.getNome(),v.getCpf(),v.getDataNasc(),v.getData(),v.getInsersor(),v.getRetorno(),v.getVacinaLote(),v.getSegundaDose()});
            } else if(pesquisa.equals("")){
                atualizarTableVacinacao(tableVacinacao);
            }
        }
    }
    
    /**
     * Método que pesquisa os registros por intervalo de datas
     * @param intervalo1v
     * @param intervalo2v
     * @param tableVacinacao
     * @return false or true
     */
    public boolean pesquisarVacinacaoPorIntervalo(String intervalo1v, String intervalo2v, DefaultTableModel tableVacinacao) {
        try {
            java.util.Date data1 = new SimpleDateFormat("MM/dd/yyyy").parse(intervalo1v);
            java.util.Date data3 = new SimpleDateFormat("MM/dd/yyyy").parse(intervalo2v);
            tableVacinacao.setRowCount(0);
            for(Vacinacao vac: listaVacinacao){
                java.util.Date data2 = new SimpleDateFormat("MM/dd/yyyy").parse(vac.getData());
                if((data1.before(data2)||data1.equals(data2))&&(data2.before(data3)||data2.equals(data3))){
                    tableVacinacao.addRow(new Object[] {vac.getNome(), vac.getCpf(), vac.getDataNasc(), vac.getData(), vac.getInsersor(), vac.getRetorno(), vac.getVacinaLote(), vac.getSegundaDose()});
                }
            }
            return false;
        } catch (ParseException ex) {
            return true;
        }
    }
    
    /**
     * Método verifica se existe um cpf na listaVacinacao
     * @param cpf
     * @return false, se existir, true, se não
     */
    public boolean compararCPFVacinado(String cpf){
        for(Vacinacao vac: listaVacinacao){
            if(cpf.equals(vac.getCpf())){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Método edita uma vacinação atualizando a listaVacinacao e atualizando a tableVacinacao
     * @param tableVacinacao
     * @param nome
     * @param cpf
     * @param dataNasc
     * @param data
     * @param insersor
     * @param retorno
     * @param vacinaLote
     * @param segundaDose
     * @param numLinha 
     */
    public void editarVacinacao(DefaultTableModel tableVacinacao, String nome, String cpf, String dataNasc, String data, String insersor, String retorno, String vacinaLote, String segundaDose, int numLinha){
        tableVacinacao.setRowCount(0);
        listaVacinacao.get(numLinha).setNome(nome);
        listaVacinacao.get(numLinha).setCpf(cpf);
        listaVacinacao.get(numLinha).setDataNasc(dataNasc);
        listaVacinacao.get(numLinha).setData(data);
        listaVacinacao.get(numLinha).setInsersor(insersor);
        listaVacinacao.get(numLinha).setRetorno(retorno);
        listaVacinacao.get(numLinha).setVacinaLote(vacinaLote);
        listaVacinacao.get(numLinha).setSegundaDose(segundaDose);
        atualizarTableVacinacao(tableVacinacao);
    }
    
    /**
     * Método exclui uma vacinação da listaVacinacao, tableVacinacao e atualiza a table
     * @param i
     * @param tableVacinacao 
     */
    public void excluirVacinacao(int i, DefaultTableModel tableVacinacao) {
        listaVacinacao.remove(i);
        tableVacinacao.removeRow(i);
        atualizarTableVacinacao(tableVacinacao);
    }
    
    /**
     * Método exporta para a db os dados da ArrayList listaVacinacao
     * @return true, se conseguir, false, se não
     */
    public boolean exportarDBVacinacao() {
        conexao = Conexao.conector();
        String sql = "INSERT INTO tb_vacinacao (nome, cpf, dataNasc, dat, insersor, retorno, vacinaLote, segundaDose) SELECT * FROM (SELECT ? AS nome, ? AS cpf, ? AS dataNasc, ? AS dat, ? AS insersor, ? AS retorno, ? AS vacinaLote, ? AS segundaDose) AS tmp WHERE NOT EXISTS (SELECT cpf FROM tb_vacinacao WHERE cpf = ?) LIMIT 1";
        try {
            pst = conexao.prepareStatement(sql);
            for (Vacinacao v: listaVacinacao) {
                pst.setString(1, v.getNome());
                pst.setString(2, v.getCpf());
                pst.setString(3, v.getDataNasc());
                pst.setString(4, v.getData());
                pst.setString(5, v.getInsersor());
                pst.setString(6, v.getRetorno());
                pst.setString(7, v.getVacinaLote());
                pst.setString(8, v.getSegundaDose());
                pst.setString(9, v.getCpf());
                //atualiza a tabela da db com os dados da arraylist
                pst.executeUpdate();
            }
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Método limpa uma vacinação da db
     * @return true, se conseguir, false, se não
     */
    public boolean deleteDBVacinacao(String cpf){
        conexao = Conexao.conector();
        String sql = "delete from tb_vacinacao where cpf = ?";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, cpf);
            pst.executeUpdate();
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Método importa da db os dados de vacinação e insere na listaVacinacao
     * @return true, se conseguir, false, se não
     */
    public boolean importarDBVacinacao() {
        conexao = Conexao.conector();
        String sql = "select * from tb_vacinacao";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            while(rs.next()){
                listaVacinacao.add(new Vacinacao(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8)));
            }
            pst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
}