package classes;
import java.sql.*;

/**Classe para conexão com o banco de dados
 * @version 1.0
 * @author Matheus Lima
 */

public class Conexao {
    
    /**
     * Método responsável por estabelecer conexão com o banco de dados
     * @return conexão, se tudo der certo; null, se não
     */
    public static Connection conector(){
        java.sql.Connection conexao = null;
        // a linha abaixo chama o driver que importei
        String driver = "com.mysql.cj.jdbc.Driver";
        // armazenando infos referentes ao banco
        
        String url = "";
        String user = "";
        String password = "";

        //estabelecendo a conexão com o banco de dados
        try {
            Class.forName(driver);
            conexao = DriverManager.getConnection(url, user, password);
            return conexao;
        } catch (Exception e) {
            return null;
        }
    }
}