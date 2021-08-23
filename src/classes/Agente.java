package classes;

/**Classe para objetos do tipo Agente
 * @version 1.0
 * @author Matheus Lima
 */
public class Agente {
    private String nome, cpf, sexo, senha;
    private boolean permissao;
    private byte[] fotoPerfil;
    

    public Agente(String nome, String cpf, String sexo, boolean permissao, String senha, byte[] fotoPerfil) {
        this.nome = nome;
        this.cpf = cpf;
        this.sexo = sexo;
        this.senha = senha;
        this.permissao = permissao;
        this.fotoPerfil = fotoPerfil;
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

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public boolean getPermissao() {
        return permissao;
    }

    public void setPermissao(boolean permissao) {
        this.permissao = permissao;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Object[] obterDados() {
        return new Object[] {nome, cpf, sexo, permissao, senha};
    }

    public boolean getPermissao(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public byte[] getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(byte[] fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
    
    @Override
    public String toString() {
        return "Agente{" + "nome=" + nome + ", cpf=" + cpf + ", sexo=" + sexo + ", senha=" + senha + ", permissao=" + permissao + '}';
    }
}
