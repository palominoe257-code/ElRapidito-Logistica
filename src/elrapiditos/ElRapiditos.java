package elrapiditos;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ElRapiditos {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Mejoras globales de UI
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception e) {
            System.out.println("No se pudo cargar el tema.");
        }

        JFrame ventanaBase = new JFrame();
        ventanaBase.setTitle("Acceso al Sistema - El Rapidito Express");
        ventanaBase.setSize(900, 580);
        ventanaBase.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ventanaBase.setLocationRelativeTo(null);
        ventanaBase.setResizable(false);

        // Probar conexion a MySQL al iniciar
        ConexionDB.getConexion();

        // Cerrar conexion al salir
        ventanaBase.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ConexionDB.cerrarConexion();
                System.exit(0);
            }
        });

        // ---- PANEL RAIZ: 2 mitades ----
        JPanel panelRaiz = new JPanel(new GridLayout(1, 2));
        ventanaBase.setContentPane(panelRaiz);

        // =====================================================
        // MITAD IZQUIERDA — Banner corporativo
        // =====================================================
        JPanel panelBanner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado rojo oscuro → rojo vivo
                GradientPaint grad = new GradientPaint(
                    0, 0,          new Color(120, 10, 10),
                    0, getHeight(), new Color(200, 30, 30)
                );
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelBanner.setLayout(new GridBagLayout());

        JPanel contenidoBanner = new JPanel();
        contenidoBanner.setLayout(new BoxLayout(contenidoBanner, BoxLayout.Y_AXIS));
        contenidoBanner.setOpaque(false);

        JLabel lblLogoBig = new JLabel("EL RAPIDITO");
        lblLogoBig.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblLogoBig.setForeground(new Color(255, 255, 255, 180));
        lblLogoBig.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMarca = new JLabel("EL RAPIDITO");
        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblMarca.setForeground(Color.WHITE);
        lblMarca.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSlogan = new JLabel("Express Logistics System");
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSlogan.setForeground(new Color(255, 200, 200));
        lblSlogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sepBanner = new JSeparator();
        sepBanner.setMaximumSize(new Dimension(160, 1));
        sepBanner.setForeground(new Color(255, 255, 255, 80));
        sepBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc1 = crearLabelBanner("-  Gestion de envios en tiempo real");
        JLabel lblDesc2 = crearLabelBanner("-  Tracking por codigo de seguridad");
        JLabel lblDesc3 = crearLabelBanner("-  Control logistico completo");

        contenidoBanner.add(lblLogoBig);
        contenidoBanner.add(Box.createVerticalStrut(10));
        contenidoBanner.add(lblMarca);
        contenidoBanner.add(Box.createVerticalStrut(4));
        contenidoBanner.add(lblSlogan);
        contenidoBanner.add(Box.createVerticalStrut(24));
        contenidoBanner.add(sepBanner);
        contenidoBanner.add(Box.createVerticalStrut(20));
        contenidoBanner.add(lblDesc1);
        contenidoBanner.add(Box.createVerticalStrut(8));
        contenidoBanner.add(lblDesc2);
        contenidoBanner.add(Box.createVerticalStrut(8));
        contenidoBanner.add(lblDesc3);

        panelBanner.add(contenidoBanner);

        // =====================================================
        // MITAD DERECHA — Formulario de login
        // =====================================================
        JPanel panelLogin = new JPanel(new GridBagLayout());
        panelLogin.setBackground(new Color(248, 250, 252));

        JPanel cardLogin = new JPanel();
        cardLogin.setLayout(new BoxLayout(cardLogin, BoxLayout.Y_AXIS));
        cardLogin.setBackground(Color.WHITE);
        cardLogin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 240), 1),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        JLabel lblBienvenido = new JLabel("Bienvenido de vuelta");
        lblBienvenido.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBienvenido.setForeground(new Color(20, 30, 48));
        lblBienvenido.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Ingrese sus credenciales para continuar");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Campo Usuario
        JLabel lblUser = new JLabel("Usuario");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(50, 65, 85));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtUser = new JTextField();
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setPreferredSize(new Dimension(320, 40));
        txtUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        txtUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Campo Contraseña
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(50, 65, 85));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setPreferredSize(new Dimension(320, 40));
        txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 225), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        txtPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Botón ingresar
        JButton btnIngresar = new JButton("Ingresar al Sistema");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setBackground(new Color(195, 20, 20));
        btnIngresar.setContentAreaFilled(false);
        btnIngresar.setOpaque(true);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVersion = new JLabel("v2.0  |  El Rapidito Express Logistics");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersion.setForeground(new Color(160, 175, 195));
        lblVersion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Ensamblar card
        cardLogin.add(lblBienvenido);
        cardLogin.add(Box.createVerticalStrut(4));
        cardLogin.add(lblSub);
        cardLogin.add(Box.createVerticalStrut(28));
        cardLogin.add(lblUser);
        cardLogin.add(Box.createVerticalStrut(6));
        cardLogin.add(txtUser);
        cardLogin.add(Box.createVerticalStrut(16));
        cardLogin.add(lblPass);
        cardLogin.add(Box.createVerticalStrut(6));
        cardLogin.add(txtPass);
        cardLogin.add(Box.createVerticalStrut(24));
        cardLogin.add(btnIngresar);
        cardLogin.add(Box.createVerticalStrut(20));
        cardLogin.add(lblVersion);

        panelLogin.add(cardLogin);

        panelRaiz.add(panelBanner);
        panelRaiz.add(panelLogin);

        ventanaBase.setVisible(true);

        // ---- Lógica de login ----
        SesionActiva sesion = SesionActiva.getInstancia();

        ActionListener accionLogin = e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            if (sesion.hacerLogin(user, pass)) {
                // Cargar datos de ejemplo si la base esta vacia
                BaseDatosEnvios.getInstancia().cargarDatosEjemplo();
                GestorLogistico gestor = new GestorLogistico(sesion, ventanaBase);
                gestor.iniciarMenu();
            } else {
                txtPass.setText("");
                txtPass.requestFocus();
                JOptionPane.showMessageDialog(ventanaBase,
                    "Credenciales incorrectas. Intente de nuevo.",
                    "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
            }
        };

        btnIngresar.addActionListener(accionLogin);
        txtPass.addActionListener(accionLogin); // Enter desde el campo contraseña
        txtUser.addActionListener(e -> txtPass.requestFocus()); // Enter en usuario → ir a contraseña
    }

    private static JLabel crearLabelBanner(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(255, 220, 220));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }
}