package classes;

/**Classe para objetos do tipo Vacina
 * @version 1.0
 * @author Matheus Lima
 */
public class Vacina {
    private String nome, lote, data, insersor, quantidade, intervalo;

    public Vacina(String nome, String lote, String data, String quantidade, String intervalo, String insersor) {
        this.nome = nome;
        this.lote = lote;
        this.data = data;
        this.quantidade = quantidade;
        this.intervalo = intervalo;
        this.insersor = insersor;
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public String getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(String intervalo) {
        this.intervalo = intervalo;
    }

    public String getInsersor() {
        return insersor;
    }

    public void setInsersor(String insersor) {
        this.insersor = insersor;
    }

}
