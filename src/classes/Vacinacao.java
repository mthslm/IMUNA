package classes;

/**Classe para objetos do tipo Vacinacao
 * @version 1.0
 * @author Matheus
 */
public class Vacinacao {
    private String nome, cpf, dataNasc, data, insersor, retorno, vacinaLote, segundaDose;

    public Vacinacao(String nome, String cpf, String dataNasc, String data, String insersor, String retorno, String vacinaLote, String segundaDose) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNasc = dataNasc;
        this.data = data;
        this.insersor = insersor;
        this.retorno = retorno;
        this.vacinaLote = vacinaLote;
        this.segundaDose = segundaDose;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(String dataNasc) {
        this.dataNasc = dataNasc;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getInsersor() {
        return insersor;
    }

    public void setInsersor(String insersor) {
        this.insersor = insersor;
    }

    public String getRetorno() {
        return retorno;
    }

    public void setRetorno(String retorno) {
        this.retorno = retorno;
    }

    public String getVacinaLote() {
        return vacinaLote;
    }

    public void setVacinaLote(String vacinaLote) {
        this.vacinaLote = vacinaLote;
    }

    public String getSegundaDose() {
        return segundaDose;
    }

    public void setSegundaDose(String segundaDose) {
        this.segundaDose = segundaDose;
    }
}
