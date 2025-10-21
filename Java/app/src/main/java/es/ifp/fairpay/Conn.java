package es.ifp.fairpay;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Conn {
    private static final String URL = "jdbc:mysql://mysql-fairpay-fairpay-bd.i.aivencloud.com:11587/fairpay_db?ssl-mode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_06D48ZfjtfPIUDBlV0P";
    private Connection conn;
    private ResultSet rs;
    private PreparedStatement pstmt;


    // Metodo para conectar a la BD
    public void conectar() {
        // Carga el driver MySQL JDBC y establece la conexion
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Metodo para cerrar la conexion a la BD
    public void desconectar() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Metodo para  registrar un usuario en la BD
    public void registrarUsuario(String usuario, String pass, String billetera) {
        if (!usuario.isEmpty() && !pass.isEmpty() && !billetera.isEmpty()) {
            new Thread(() -> {
                try {
                    conectar();
                    if (conn != null) {
                        // Query SQL con PreparedStatements
                        String sql = "INSERT INTO usuarios (usuario, password, billetera) VALUES (?,?,?)";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, usuario);
                        pstmt.setString(2, pass);
                        pstmt.setString(3, billetera);
                        pstmt.executeUpdate();
                    }
                } catch (Exception e) {
                    //Lanza RuntimeException(e);
                    Log.d("", e.toString());
                } finally {
                    desconectar();
                }
            }).start();
        }
    }

   // Metodo para obtener el contenido de una tabla
   public ResultSet verTabla(String tabla) {

        if (!tabla.isEmpty()) {

                try {
                    conectar();

                    if (conn != null) {
                        // Query SQL con PreparedStatements
                        String sql = "SELECT * FROM " + tabla;
                        pstmt = conn.prepareStatement(sql);
                        //pstmt.setString(1, tabla);
                        rs = pstmt.executeQuery();
                    }
                } catch (Exception e) {
                    //Lanza RuntimeException(e);
                    Log.d("", e.toString());
                }
        }
        return rs;
    }
}
