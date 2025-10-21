package es.ifp.fairpay.database;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://mysql-fairpay-fairpay-bd.i.aivencloud.com:11587/fairpay_db?ssl-mode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_06D48ZfjtfPIUDBlV0P";

    // Metodo para registrar un usuario en la BD
    public void registrarUsuario(String usuario, String pass, String billetera) {
        if (usuario == null || usuario.isEmpty() || pass == null || pass.isEmpty() || billetera == null || billetera.isEmpty()) {
            return;
        }
        new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                String sql = "INSERT INTO usuarios (usuario, password, billetera) VALUES (?,?,?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, usuario);
                pstmt.setString(2, pass);
                pstmt.setString(3, billetera);
                pstmt.executeUpdate();
                Log.d("FairPayDB", "Usuario registrado correctamente.");
            } catch (SQLException e) {
                Log.e("FairPayDB", "Error al registrar usuario", e);
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                } catch (SQLException e) { /* ignored */ }
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) { /* ignored */ }
            }
        }).start();
    }

    // Interfaz para procesar los resultados de la consulta
    public interface ResultSetProcessor {
        void process(ResultSet rs) throws SQLException;
    }

    // Metodo para obtener el contenido de una tabla y procesarlo
    public void verTabla(String tabla, ResultSetProcessor processor) {
        if (tabla == null || tabla.isEmpty()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "SELECT * FROM " + tabla;
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            processor.process(rs);
        } catch (SQLException e) {
            Log.e("FairPayDB", "Error al ver la tabla " + tabla, e);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) { /* ignored */ }
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) { /* ignored */ }
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) { /* ignored */ }
        }
    }
}
