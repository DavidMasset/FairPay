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


    /* Interfaz para procesar los resultados de la consulta */
    public interface ResultSetProcessor {
        void process(ResultSet rs) throws SQLException;
    }

    // Metodo para obtener el contenido de una tabla y procesarlo
    public void obtenerUsuarios(ResultSetProcessor processor) {
        new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                String sql = "SELECT id_usuario, nombre, apellidos, correo FROM Usuario";

                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();
                processor.process(rs);
            } catch (SQLException e) {
                Log.e("FairPayDB", "Error al obtener usuarios. ", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (SQLException e) { Log.e("FairPayDB","Error al cerrar rs.",e);}
                try {
                    if (pstmt != null) pstmt.close();
                } catch (SQLException e) { Log.e("FairPayDB","Error al cerrar pstmt.",e);}
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) { Log.e("FairPayDB","Error al cerrar rs.",e); }
            }
        }).start();
    }
}