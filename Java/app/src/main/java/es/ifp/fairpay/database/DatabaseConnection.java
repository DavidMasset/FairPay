package es.ifp.fairpay.database;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://mysql-fairpay-fairpay-bd.i.aivencloud.com:11587/fairpay_db?ssl-mode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_06D48ZfjtfPIUDBlV0P";


    /* Interfaz para procesar los resultados de la consulta */
    public interface ResultSetProcessor {
        void process(ResultSet rs) throws SQLException;
    }

    public void registrarUsuario(String nombre, String apellidos, String correo, String contrasena_hash, String telefono, String direccionBilletera, String clavePrivadaCifrada) {
        if (nombre == null || nombre.isEmpty() || apellidos == null || apellidos.isEmpty() || correo == null || correo.isEmpty() || contrasena_hash == null || contrasena_hash.isEmpty() || direccionBilletera == null || direccionBilletera.isEmpty()) {
            Log.w("FairPayDB", "Intento de registro con datos esenciales incompletos.");
            return;
        }

        new Thread(() -> {

            Connection conn = null;
            PreparedStatement pstmtUsuario = null;
            PreparedStatement pstmtBilletera = null;
            ResultSet generatedKeys = null;

            try {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false); //Se desactiva el auto commit para poder hacer un commit manual una vez que se han insertado todos los datos.

                String sqlUsuario = "INSERT INTO Usuario (nombre, apellidos,correo, contraseña_hash, telefono) VALUES (?, ?, ?, ?, ?)";
                pstmtUsuario = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS); //Devuelve los vlaores de ID del Autoincrement
                pstmtUsuario.setString(1, nombre);
                pstmtUsuario.setString(2, apellidos);
                pstmtUsuario.setString(3, correo);
                pstmtUsuario.setString(4, contrasena_hash);
                pstmtUsuario.setString(5, telefono);
                if (pstmtUsuario.executeUpdate() == 0) {
                    throw new SQLException("La creación del usuario falló, no se pudo insertar el usuario.");
                }
                /*Para obtener las ids generadas*/
                generatedKeys = pstmtUsuario.getGeneratedKeys();
                long idUsuario;
                if (generatedKeys.next()) {
                    idUsuario = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("La creación del usuario falló, no se pudo obtener el ID.");
                }


                String sqlBilletera = "INSERT INTO Billetera (dirección, clave_privada_cifrada, id_usuario) VALUES (?, ?, ?)";
                pstmtBilletera = conn.prepareStatement(sqlBilletera);
                pstmtBilletera.setString(1, direccionBilletera);
                pstmtBilletera.setString(2, clavePrivadaCifrada);
                pstmtBilletera.setLong(3, idUsuario);
                pstmtBilletera.executeUpdate();

                conn.commit();
                Log.d("FairPayDB", "TRANSACCIÓN COMPLETADA: Usuario " + nombre + " y su billetera han sido registrados.");


            } catch (SQLException e) {
                Log.e("FairPayDB", "Errorr transacción", e);
                try {
                    conn.rollback();
                } catch (SQLException eb) {
                    Log.e("FairPayDB", "Error al revertir la transacción", eb);
                }
            } finally {
                try {
                    if (generatedKeys != null) generatedKeys.close();
                } catch (SQLException e) {
                    Log.e("FairPayDB", "Error al cerrar rs.", e);
                }
                try {
                    if (pstmtBilletera != null) pstmtBilletera.close();
                } catch (SQLException e) {
                    Log.e("FairPayDB", "Error al cerrar pstmt.", e);
                }
                try {
                    if (pstmtUsuario != null) pstmtUsuario.close();
                } catch (SQLException e) {
                    Log.e("FairPayDB", "Error al cerrar pstmt.", e);
                }
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();

                    }
                } catch (SQLException e) {
                    Log.e("FairPayDB", "Error al cerrar rs.", e);
                }
            }
        }).start();
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
                } catch (SQLException e) {
                    Log.e("FairPayDB", "Error al cerrar rs.", e);
                }
                try {
                    if (pstmt != null) pstmt.close();
                } catch (SQLException e) {
                    Log.e("FairPayDB", "Error al cerrar pstmt.", e);
                }
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    Log.e("FairPayDB", "Error al cerrar rs.", e);
                }
            }
        }).start();
    }
}