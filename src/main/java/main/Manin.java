package main;

import java.sql.*;

public class Manin {
    public static void main(String[] args) {
        Connection con = conectarMySQL();




        try {
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String driver = "com.mysql.cj.jdbc.Driver";
    public static String database = "yuan";
    public static String hostname = "10.128.21.111";
    public static String port = "3306";
    public static String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=false";
    public static String username = "root";
    public static String password = "pito";

    public static Connection conectarMySQL() {
        Connection conn = null;

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }
}


/* DBDsion

Que acomete la hacecion de falta:

    + cuentas de usuario
    + datos de cuenta(pj, runas...)
    + PJ
    + Runa
    - relacion serie-rutainteran del video almacenado
    - anime (names, tags, generos)???
    - relacion anime - secuencia (op, insert, ending)
    - partidas empezadas sin acabar
    - relacion partida - jugador
    - la pikadura de la cobra GaySON

    # Shareado en toda la aplicaçao
    Runa
    ID      Nombre      Tier        Descripcion

    PJ
    ID  HP  ATK  DEF  Nombre  Ruta_portrait


    # User-specific
    Usuario
    ID    Username    PWD(SHA256 porfa)    Correo      MALUser

    Sesion
    ID_User     Token       Expires

    Inventario
    ID_Usuario      ID_Runa     Cantidad

    Layout
    ID_Usuario      ID_PJ       <XML o JSON con el layout>

    Top
    ID_Usuario      Diff        Floor


*/