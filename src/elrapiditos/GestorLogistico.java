package elrapiditos;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * GRASP: Controlador principal de la UI.
 * Sin emojis. Precios calculados por distancia via CalculadorTarifa.
 */
public class GestorLogistico implements ActionListener {

    // -----------------------------------------------------------------------
    // Dependencias
    // -----------------------------------------------------------------------
    private final SesionActiva    sesion;
    private final JFrame          ventanaBase;
    private final BaseDatosEnvios baseDatos;

    // -----------------------------------------------------------------------
    // Navegacion
    // -----------------------------------------------------------------------
    private JPanel     workspaceDerecho;
    private CardLayout navegadorPaneles;

    // -----------------------------------------------------------------------
    // Modelos de tablas
    // -----------------------------------------------------------------------
    private DefaultTableModel modeloTablaHistorial;
    private DefaultTableModel modeloTablaResultados;

    // -----------------------------------------------------------------------
    // Labels de estadisticas (dashboard)
    // -----------------------------------------------------------------------
    private JLabel lblStatTotal;
    private JLabel lblStatActivos;
    private JLabel lblStatIngresos;
    // Labels de conteo por estado
    private JLabel lblEstPendiente;
    private JLabel lblEstAlmacen;
    private JLabel lblEstTransito;
    private JLabel lblEstEntregado;
    private JLabel lblEstCancelado;
    // Timer de actualizacion automatica del dashboard
    private Timer  timerDashboard;
    private JLabel lblUltimaActualizacion;

    // -----------------------------------------------------------------------
    // Paleta de colores
    // -----------------------------------------------------------------------
    private static final Color C_ROJO       = new Color(195, 20, 20);
    private static final Color C_VERDE      = new Color(22, 163, 74);
    private static final Color C_AMARILLO   = new Color(202, 138, 4);
    private static final Color C_SIDEBAR_BG = new Color(17, 24, 39);
    private static final Color C_SIDEBAR_BTN= new Color(31, 41, 55);
    private static final Color C_FONDO      = new Color(243, 244, 246);
    private static final Color C_BLANCO     = Color.WHITE;
    private static final Color C_TEXTO      = new Color(17, 24, 39);
    private static final Color C_MUTED      = new Color(107, 114, 128);
    private static final Color C_BORDE      = new Color(209, 213, 219);
    private static final Color C_INPUT_BG   = new Color(249, 250, 251);

    // Lista de agencias disponibles (centralizada)
    private static final String[] AGENCIAS = {
        "Lima Central (CAPITAL)", "Lima Norte", "Lima Sur", "Callao",
        "Arequipa", "Trujillo", "Piura", "Cusco",
        "Chiclayo", "Iquitos", "Tacna", "Puno"
    };

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public GestorLogistico(SesionActiva sesion, JFrame ventanaBase) {
        this.sesion      = sesion;
        this.ventanaBase = ventanaBase;
        this.baseDatos   = BaseDatosEnvios.getInstancia();
    }

    // -----------------------------------------------------------------------
    // INICIAR INTERFAZ
    // -----------------------------------------------------------------------
    public void iniciarMenu() {
        ventanaBase.setTitle("El Rapidito — Sistema Logistico");
        ventanaBase.setSize(1200, 720);
        ventanaBase.setLocationRelativeTo(null);
        ventanaBase.setMinimumSize(new Dimension(1000, 600));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(C_FONDO);
        raiz.add(construirSidebar(),   BorderLayout.WEST);
        raiz.add(construirWorkspace(), BorderLayout.CENTER);

        ventanaBase.setContentPane(raiz);
        ventanaBase.revalidate();
        ventanaBase.repaint();

        // Timer de actualizacion automatica cada 4 segundos
        timerDashboard = new Timer(4000, e -> refrescarEstadisticas());
        timerDashboard.start();
    }

    // -----------------------------------------------------------------------
    // SIDEBAR
    // -----------------------------------------------------------------------
    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(C_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(11, 16, 26));
        header.setBorder(new EmptyBorder(26, 18, 20, 18));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel lblLogo = new JLabel("El Rapidito");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblOp = new JLabel(sesion.getNombreUsuario() + "  |  " + sesion.getRol());
        lblOp.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblOp.setForeground(new Color(156, 163, 175));
        lblOp.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(lblLogo);
        header.add(Box.createVerticalStrut(3));
        header.add(lblOp);
        sidebar.add(header);

        // Menu items
        sidebar.add(crearSeccionNav("MENU"));
        sidebar.add(crearItemNav("  Dashboard",            "PANEL_INICIO"));
        sidebar.add(crearItemNav("  Nuevo Envio",          "PANEL_NUEVO"));
        sidebar.add(crearItemNav("  Historial de Envios",  "PANEL_HISTORIAL"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(crearSeccionNav("OPERACIONES"));
        sidebar.add(crearItemNav("  Buscar Envio",         "PANEL_BUSQUEDA"));
        sidebar.add(crearItemNav("  Actualizar Estado",    "PANEL_ESTADO"));
        sidebar.add(crearItemNav("  Cancelar Envio",       "PANEL_CANCELAR"));
        sidebar.add(Box.createVerticalGlue());

        // Footer
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(new Color(11, 16, 26));
        footer.setBorder(new EmptyBorder(12, 12, 12, 12));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JButton btnSalir = new JButton("Cerrar Sesion");
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalir.setForeground(new Color(252, 165, 165));
        btnSalir.setBackground(new Color(30, 15, 15));
        btnSalir.setContentAreaFilled(false);
        btnSalir.setOpaque(true);
        btnSalir.setBorderPainted(false);
        btnSalir.setBorder(new EmptyBorder(10, 14, 10, 14));
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSalir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSalir.setHorizontalAlignment(SwingConstants.LEFT);
        btnSalir.setActionCommand("CERRAR_SESION");
        btnSalir.addActionListener(this);
        footer.add(btnSalir);
        sidebar.add(footer);

        return sidebar;
    }

    private JLabel crearSeccionNav(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(75, 85, 99));
        lbl.setBorder(new EmptyBorder(14, 18, 4, 18));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return lbl;
    }

    private JButton crearItemNav(String texto, String comando) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(209, 213, 219));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setActionCommand(comando);
        btn.addActionListener(this);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent me) {
                btn.setOpaque(true);
                btn.setBackground(C_SIDEBAR_BTN);
                btn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent me) {
                btn.setOpaque(false);
                btn.setForeground(new Color(209, 213, 219));
            }
        });
        return btn;
    }

    // -----------------------------------------------------------------------
    // WORKSPACE
    // -----------------------------------------------------------------------
    private JPanel construirWorkspace() {
        navegadorPaneles = new CardLayout();
        workspaceDerecho = new JPanel(navegadorPaneles);
        workspaceDerecho.setBackground(C_FONDO);

        workspaceDerecho.add(crearPanelInicio(),            "INICIO");
        workspaceDerecho.add(crearPanelFormularioPedido(),  "NUEVO");
        workspaceDerecho.add(crearPanelHistorial(),         "HISTORIAL");
        workspaceDerecho.add(crearPanelBusqueda(),          "BUSQUEDA");
        workspaceDerecho.add(crearPanelActualizarEstado(),  "ESTADO");
        workspaceDerecho.add(crearPanelCancelarEnvio(),     "CANCELAR");

        return workspaceDerecho;
    }

    // -----------------------------------------------------------------------
    // PANEL: DASHBOARD
    // -----------------------------------------------------------------------
    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(C_FONDO);
        panel.setBorder(new EmptyBorder(24, 32, 24, 32));

        // --- Encabezado ---
        JPanel enc = new JPanel(new BorderLayout());
        enc.setOpaque(false);

        JLabel lblTit = new JLabel("Dashboard");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTit.setForeground(C_TEXTO);

        lblUltimaActualizacion = new JLabel("En vivo — actualizacion automatica");
        lblUltimaActualizacion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUltimaActualizacion.setForeground(C_VERDE);

        JLabel lblSub = new JLabel("Resumen completo del sistema logistico  |  " + sesion.getRol());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_MUTED);

        JPanel encNorte = new JPanel(new BorderLayout());
        encNorte.setOpaque(false);
        encNorte.add(lblTit, BorderLayout.WEST);
        encNorte.add(lblUltimaActualizacion, BorderLayout.EAST);

        enc.add(encNorte, BorderLayout.NORTH);
        enc.add(lblSub,   BorderLayout.SOUTH);
        panel.add(enc, BorderLayout.NORTH);

        // --- Panel central: filas de tarjetas ---
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);

        // Fila 1: 3 tarjetas grandes (totales)
        JPanel fila1 = new JPanel(new GridLayout(1, 3, 14, 0));
        fila1.setOpaque(false);
        fila1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblStatTotal    = new JLabel("0");
        lblStatActivos  = new JLabel("0");
        lblStatIngresos = new JLabel("S/. 0.00");

        fila1.add(crearCardStat("Total de Pedidos",       lblStatTotal,    C_ROJO,     "Todos los envios registrados",
            () -> abrirDialogoFiltrado("Todos los Pedidos", baseDatos.obtenerTodos(), null)));
        fila1.add(crearCardStat("Pedidos Activos",         lblStatActivos,  C_VERDE,    "Registrado / En proceso",
            () -> abrirDialogoFiltrado("Pedidos Activos",
                baseDatos.obtenerTodos().stream()
                    .filter(r -> r.getEstado() != RegistroEnvio.EstadoEnvio.CANCELADO
                              && r.getEstado() != RegistroEnvio.EstadoEnvio.ENTREGADO)
                    .collect(java.util.stream.Collectors.toList()), null)));
        fila1.add(crearCardStat("Ingresos Brutos Totales", lblStatIngresos, C_AMARILLO, "Suma de todos los pedidos — clic para ver",
            () -> abrirDialogoFiltrado("Ingresos Brutos Totales", baseDatos.obtenerTodos(), C_AMARILLO)));
        centro.add(fila1);
        centro.add(Box.createVerticalStrut(14));

        // Separador de seccion
        JLabel lblSecEst = new JLabel("ESTADO DE LOS ENVIOS EN TIEMPO REAL");
        lblSecEst.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSecEst.setForeground(C_MUTED);
        lblSecEst.setAlignmentX(Component.LEFT_ALIGNMENT);
        centro.add(lblSecEst);
        centro.add(Box.createVerticalStrut(8));

        // Fila 2: 5 tarjetas de estado (simplificadas)
        JPanel fila2 = new JPanel(new GridLayout(1, 5, 10, 0));
        fila2.setOpaque(false);
        fila2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblEstPendiente  = new JLabel("0");
        lblEstAlmacen    = new JLabel("0");
        lblEstTransito   = new JLabel("0");
        lblEstEntregado  = new JLabel("0");
        lblEstCancelado  = new JLabel("0");

        fila2.add(crearCardEstado("Pendiente",   lblEstPendiente,  new Color(99, 102, 241),
            () -> abrirDialogoFiltrado("Pendiente de Recojo",
                baseDatos.filtrarPorEstado(RegistroEnvio.EstadoEnvio.PENDIENTE), new Color(99, 102, 241))));
        fila2.add(crearCardEstado("En Almacen",  lblEstAlmacen,    new Color(234, 179, 8),
            () -> abrirDialogoFiltrado("En Almacen",
                baseDatos.filtrarPorEstado(RegistroEnvio.EstadoEnvio.EN_ALMACEN), new Color(234, 179, 8))));
        fila2.add(crearCardEstado("En Transito", lblEstTransito,   new Color(59, 130, 246),
            () -> abrirDialogoFiltrado("En Transito",
                baseDatos.filtrarPorEstado(RegistroEnvio.EstadoEnvio.EN_TRANSITO), new Color(59, 130, 246))));
        fila2.add(crearCardEstado("Entregado",   lblEstEntregado,  C_VERDE,
            () -> abrirDialogoFiltrado("Entregados",
                baseDatos.filtrarPorEstado(RegistroEnvio.EstadoEnvio.ENTREGADO), C_VERDE)));
        fila2.add(crearCardEstado("Cancelado",   lblEstCancelado,  C_ROJO,
            () -> abrirDialogoFiltrado("Cancelados",
                baseDatos.filtrarPorEstado(RegistroEnvio.EstadoEnvio.CANCELADO), C_ROJO)));
        centro.add(fila2);

        panel.add(centro, BorderLayout.CENTER);

        // --- Acciones rapidas ---
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        acciones.setOpaque(false);
        JButton btnNuevo = crearBoton("+ Nuevo Envio",  C_ROJO, C_BLANCO);
        btnNuevo.addActionListener(e -> navegadorPaneles.show(workspaceDerecho, "NUEVO"));
        JButton btnHist  = crearBoton("Ver Historial",  new Color(240, 242, 245), C_TEXTO);
        btnHist.addActionListener(e -> navegadorPaneles.show(workspaceDerecho, "HISTORIAL"));
        JButton btnBuscar = crearBoton("Buscar Envio", new Color(240, 242, 245), C_TEXTO);
        btnBuscar.addActionListener(e -> navegadorPaneles.show(workspaceDerecho, "BUSQUEDA"));
        acciones.add(btnNuevo);
        acciones.add(btnHist);
        acciones.add(btnBuscar);
        panel.add(acciones, BorderLayout.SOUTH);

        return panel;
    }

    /** Tarjeta compacta clicable para mostrar conteo de un estado especifico */
    private JPanel crearCardEstado(String titulo, JLabel lblValor, Color color, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(C_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDE, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Barra de color superior
        JPanel topBar = new JPanel();
        topBar.setBackground(color);
        topBar.setPreferredSize(new Dimension(0, 3));
        card.add(topBar, BorderLayout.NORTH);

        JLabel lblT = new JLabel(titulo, JLabel.CENTER);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblT.setForeground(C_MUTED);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(color);
        lblValor.setHorizontalAlignment(JLabel.CENTER);

        card.add(lblT,     BorderLayout.CENTER);
        card.add(lblValor, BorderLayout.SOUTH);

        // Hover + click
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) { onClick.run(); }
            @Override public void mouseEntered(MouseEvent me) {
                card.setBackground(new Color(248, 250, 252));
                card.revalidate(); card.repaint();
            }
            @Override public void mouseExited(MouseEvent me) {
                card.setBackground(C_BLANCO);
                card.revalidate(); card.repaint();
            }
        });
        return card;
    }

    private JPanel crearCardStat(String titulo, JLabel lblValor, Color accent, String subtitulo, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(C_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblT.setForeground(C_TEXTO);
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(C_MUTED);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblValor.setForeground(accent);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblT,   BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);

        card.add(header,   BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        // Hover + click
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) { onClick.run(); }
            @Override public void mouseEntered(MouseEvent me) {
                card.setBackground(new Color(252, 252, 253));
                card.revalidate(); card.repaint();
            }
            @Override public void mouseExited(MouseEvent me) {
                card.setBackground(C_BLANCO);
                card.revalidate(); card.repaint();
            }
        });
        return card;
    }

    private void refrescarEstadisticas() {
        if (lblStatTotal == null) return;
        // Totales generales
        lblStatTotal.setText(String.valueOf(baseDatos.totalEnvios()));
        lblStatActivos.setText(String.valueOf(baseDatos.totalActivos()));
        lblStatIngresos.setText(String.format("S/. %.2f", baseDatos.calcularIngresosTotal()));
        // Conteo por estado (simplificado)
        lblEstPendiente.setText(String.valueOf(
            baseDatos.contarPorEstado(RegistroEnvio.EstadoEnvio.PENDIENTE)));
        lblEstAlmacen.setText(String.valueOf(
            baseDatos.contarPorEstado(RegistroEnvio.EstadoEnvio.EN_ALMACEN)));
        lblEstTransito.setText(String.valueOf(
            baseDatos.contarPorEstado(RegistroEnvio.EstadoEnvio.EN_TRANSITO)));
        lblEstEntregado.setText(String.valueOf(
            baseDatos.contarPorEstado(RegistroEnvio.EstadoEnvio.ENTREGADO)));
        lblEstCancelado.setText(String.valueOf(
            baseDatos.contarPorEstado(RegistroEnvio.EstadoEnvio.CANCELADO)));
        // Indicador de actualizacion
        if (lblUltimaActualizacion != null) {
            lblUltimaActualizacion.setText("En vivo — actualizado");
        }
    }

    // -----------------------------------------------------------------------
    // PANEL: FORMULARIO NUEVO ENVIO — sin emojis + precios por distancia
    // -----------------------------------------------------------------------
    private JPanel crearPanelFormularioPedido() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(C_FONDO);

        // Encabezado fijo
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(C_BLANCO);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDE),
            BorderFactory.createEmptyBorder(18, 32, 18, 32)
        ));
        JLabel lblTit = new JLabel("Registrar Nuevo Envio");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTit.setForeground(C_TEXTO);
        JLabel lblSub = new JLabel("Complete todos los campos para generar el comprobante y codigo de seguridad.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_MUTED);
        encabezado.add(lblTit, BorderLayout.NORTH);
        encabezado.add(lblSub, BorderLayout.SOUTH);
        contenedor.add(encabezado, BorderLayout.NORTH);

        // Cuerpo scrolleable
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBackground(C_FONDO);
        cuerpo.setBorder(new EmptyBorder(20, 32, 20, 32));

        // === SECCION 1: DETALLES DEL PAQUETE ===
        cuerpo.add(crearEncabezadoSeccion("Detalles del Paquete", C_ROJO));
        cuerpo.add(Box.createVerticalStrut(10));

        JComboBox<String> cboOrigen  = crearCombo(AGENCIAS);
        JComboBox<String> cboDestino = crearCombo(AGENCIAS);
        cuerpo.add(crearFila2Col("Agencia de Origen:", cboOrigen, "Agencia de Destino:", cboDestino));
        cuerpo.add(Box.createVerticalStrut(10));

        JTextField txtContenido = crearInput();
        JTextField txtPeso      = crearInput();
        cuerpo.add(crearFila2Col("Contenido / Articulo:", txtContenido, "Peso Total (kg):", txtPeso));
        cuerpo.add(Box.createVerticalStrut(8));

        // Info de tarifa (se actualiza cuando cambian origen/destino o peso)
        JLabel lblTarifaInfo = new JLabel("Seleccione origen y destino para ver la tarifa aplicable.");
        lblTarifaInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTarifaInfo.setForeground(C_MUTED);
        lblTarifaInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTarifaInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        cuerpo.add(lblTarifaInfo);
        cuerpo.add(Box.createVerticalStrut(10));

        // Servicios adicionales
        JCheckBox chkSeguro  = new JCheckBox("Seguro de Carga  (+15% del valor)");
        JCheckBox chkExpress = new JCheckBox("Entrega Express  (+S/. 20.00)");
        estilizarCheckbox(chkSeguro);
        estilizarCheckbox(chkExpress);
        cuerpo.add(crearFilaCheckboxes(chkSeguro, chkExpress));
        cuerpo.add(Box.createVerticalStrut(10));

        // Metodo de pago
        String[] metodosPago = {"Efectivo", "Yape / Plin", "Tarjeta de Debito/Credito", "Transferencia Bancaria"};
        JComboBox<String> cboMetodoPago = crearCombo(metodosPago);
        cuerpo.add(crearFila2Col("Metodo de Pago:", cboMetodoPago,
                                 "Servicios:", new JPanel() {{ setOpaque(false); }}));
        // reemplazar el panel vacio con los checkboxes en fila distinta - ajuste:
        cuerpo.add(Box.createVerticalStrut(22));

        // === SECCION 2: DATOS DE PERSONAS ===
        cuerpo.add(crearEncabezadoSeccion("Datos de las Partes", C_TEXTO));
        cuerpo.add(Box.createVerticalStrut(10));

        JPanel subHeaders = new JPanel(new GridLayout(1, 2, 16, 0));
        subHeaders.setOpaque(false);
        subHeaders.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        subHeaders.add(crearSubHeader("REMITENTE",    new Color(59, 130, 246)));
        subHeaders.add(crearSubHeader("DESTINATARIO", new Color(16, 185, 129)));
        cuerpo.add(subHeaders);
        cuerpo.add(Box.createVerticalStrut(8));

        JTextField txtRemitente    = crearInput();
        JTextField txtDniRem       = crearInput();
        JTextField txtCelRem       = crearInput();
        JTextField txtDestinatario = crearInput();
        JTextField txtDniDest      = crearInput();
        JTextField txtCelDest      = crearInput();

        JPanel cardPersonas = new JPanel(new GridLayout(1, 2, 16, 0));
        cardPersonas.setOpaque(false);
        cardPersonas.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        cardPersonas.add(crearColumnaPersona(
            new String[]{"Nombre Completo:", "DNI / Documento:", "Celular:"},
            new JComponent[]{txtRemitente, txtDniRem, txtCelRem},
            new Color(59, 130, 246)
        ));
        cardPersonas.add(crearColumnaPersona(
            new String[]{"Nombre Completo:", "DNI / Documento:", "Celular:"},
            new JComponent[]{txtDestinatario, txtDniDest, txtCelDest},
            new Color(16, 185, 129)
        ));
        cuerpo.add(cardPersonas);
        cuerpo.add(Box.createVerticalStrut(22));

        // === PIE: Precio + Botones ===
        JPanel pie = new JPanel(new BorderLayout(16, 0));
        pie.setOpaque(false);
        pie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblCosto = new JLabel("Costo estimado: S/. 0.00");
        lblCosto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCosto.setForeground(C_VERDE);

        JPanel botonesForm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesForm.setOpaque(false);
        JButton btnVerPrecio = crearBoton("Ver Precio",       new Color(240, 242, 245), C_TEXTO);
        JButton btnRegistrar = crearBoton("Registrar Envio",  C_ROJO, C_BLANCO);
        botonesForm.add(btnVerPrecio);
        botonesForm.add(btnRegistrar);

        pie.add(lblCosto,    BorderLayout.WEST);
        pie.add(botonesForm, BorderLayout.EAST);
        cuerpo.add(pie);

        JScrollPane scroll = new JScrollPane(cuerpo);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        contenedor.add(scroll, BorderLayout.CENTER);

        // ---- Auto-actualizar info de tarifa cuando cambia origen/destino ----
        ItemListener actualizarTarifa = e2 -> {
            String o = (String) cboOrigen.getSelectedItem();
            String d = (String) cboDestino.getSelectedItem();
            lblTarifaInfo.setText(CalculadorTarifa.getEtiquetaTarifa(o, d));
            lblCosto.setText("Costo estimado: S/. 0.00");
        };
        cboOrigen.addItemListener(actualizarTarifa);
        cboDestino.addItemListener(actualizarTarifa);

        // ---- Ver Precio ----
        btnVerPrecio.addActionListener(e -> {
            try {
                double peso = Double.parseDouble(txtPeso.getText().trim());
                if (peso <= 0) { mostrarError("El peso debe ser mayor a 0."); return; }
                String o = (String) cboOrigen.getSelectedItem();
                String d = (String) cboDestino.getSelectedItem();
                IEnvio tmp = EnvioFactory.crearPaquete("x", peso, o, d);
                if (chkSeguro.isSelected())  tmp = new SeguroDecorator(tmp);
                if (chkExpress.isSelected()) tmp = new ExpressDecorator(tmp);
                lblCosto.setText(String.format("Costo estimado: S/. %.2f", tmp.calcularCosto()));
                lblTarifaInfo.setText(CalculadorTarifa.getEtiquetaTarifa(o, d));
            } catch (NumberFormatException ex) {
                mostrarError("Ingrese un peso numerico valido (ej: 2.5)");
            }
        });

        // ---- Registrar envio ----
        btnRegistrar.addActionListener(e -> {
            try {
                String contenido    = txtContenido.getText().trim();
                String remitente    = txtRemitente.getText().trim();
                String dniRem       = txtDniRem.getText().trim();
                String celRem       = txtCelRem.getText().trim();
                String destinatario = txtDestinatario.getText().trim();
                String dniDest      = txtDniDest.getText().trim();
                String celDest      = txtCelDest.getText().trim();
                String origen       = (String) cboOrigen.getSelectedItem();
                String destino      = (String) cboDestino.getSelectedItem();
                double peso         = Double.parseDouble(txtPeso.getText().trim());

                if (contenido.isEmpty() || remitente.isEmpty() || destinatario.isEmpty()) {
                    mostrarError("Complete los campos obligatorios:\nContenido, Remitente y Destinatario.");
                    return;
                }
                if (peso <= 0) { mostrarError("El peso debe ser mayor a 0 kg."); return; }

                // Crear paquete con tarifa segun distancia
                IEnvio paquete   = EnvioFactory.crearPaquete(contenido, peso, origen, destino);
                CajaComposite caja = new CajaComposite("Paquete");
                caja.agregarEnvio(paquete);

                IEnvio envioFinal = caja;
                if (chkSeguro.isSelected())  envioFinal = new SeguroDecorator(envioFinal);
                if (chkExpress.isSelected()) envioFinal = new ExpressDecorator(envioFinal);

                RegistroEnvio reg = baseDatos.registrarEnvio(
                    envioFinal,
                    remitente, dniRem, celRem,
                    destinatario, dniDest, celDest,
                    origen, destino
                );
                // Guardar metodo de pago seleccionado
                reg.setMetodoPago((String) cboMetodoPago.getSelectedItem());

                modeloTablaHistorial.addRow(filaDeRegistro(reg));
                refrescarEstadisticas();
                abrirVentanaBoletaTracking(reg);

                // Limpiar formulario
                txtContenido.setText("");
                txtPeso.setText("");
                txtRemitente.setText(""); txtDniRem.setText(""); txtCelRem.setText("");
                txtDestinatario.setText(""); txtDniDest.setText(""); txtCelDest.setText("");
                chkSeguro.setSelected(false); chkExpress.setSelected(false);
                lblCosto.setText("Costo estimado: S/. 0.00");
                cboOrigen.setSelectedIndex(0);
                cboDestino.setSelectedIndex(0);
                cboMetodoPago.setSelectedIndex(0);
                navegadorPaneles.show(workspaceDerecho, "HISTORIAL");

            } catch (NumberFormatException ex) {
                mostrarError("Ingrese un peso numerico valido (ej: 15)");
            }
        });

        return contenedor;
    }

    // -----------------------------------------------------------------------
    // PANEL: HISTORIAL
    // -----------------------------------------------------------------------
    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_FONDO);
        panel.add(crearEncabezadoPanel("Historial de Envios",
            "Haz clic en una fila para ver el comprobante y seguimiento."), BorderLayout.NORTH);

        String[] cols = {"ID", "Codigo", "Remitente", "Destinatario", "Ruta", "Importe", "Estado", "Fecha"};
        modeloTablaHistorial = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Pre-cargar todos los registros existentes (incluye datos de ejemplo)
        baseDatos.obtenerTodos().forEach(r -> modeloTablaHistorial.addRow(filaDeRegistro(r)));

        JTable tabla = crearTabla(modeloTablaHistorial);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(85);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(115);

        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int f = tabla.getSelectedRow();
                if (f != -1) {
                    RegistroEnvio reg = baseDatos.buscarPorCodigo(
                        (String) modeloTablaHistorial.getValueAt(f, 1));
                    if (reg != null) abrirVentanaBoletaTracking(reg);
                }
            }
        });
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // -----------------------------------------------------------------------
    // DIALOGO FILTRADO — se abre al hacer clic en tarjetas del dashboard
    // -----------------------------------------------------------------------
    /**
     * Abre un dialogo con tabla de los registros dados.
     * Si accentColor != null, muestra fila de totales al pie.
     */
    private void abrirDialogoFiltrado(String titulo, List<RegistroEnvio> lista, Color accentColor) {
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(ventanaBase,
                "No hay envios en la categoria: " + titulo,
                titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(ventanaBase, titulo, true);
        dlg.setSize(900, 480);
        dlg.setLocationRelativeTo(ventanaBase);

        JPanel raiz = new JPanel(new BorderLayout(0, 0));
        raiz.setBackground(C_FONDO);

        // Cabecera del dialogo
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(accentColor != null ? accentColor : C_SIDEBAR_BG);
        cab.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel lblT = new JLabel(titulo + "  —  " + lista.size() + " envio(s)");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblT.setForeground(Color.WHITE);
        JLabel lblHint = new JLabel("Clic en una fila para ver boleta");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(new Color(255, 255, 255, 180));
        cab.add(lblT,    BorderLayout.WEST);
        cab.add(lblHint, BorderLayout.EAST);
        raiz.add(cab, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Codigo", "Remitente", "Destinatario", "Ruta", "Importe", "Estado", "Fecha"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        lista.forEach(r -> modelo.addRow(filaDeRegistro(r)));
        JTable tabla = crearTabla(modelo);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                int f = tabla.getSelectedRow();
                if (f != -1) {
                    RegistroEnvio reg = baseDatos.buscarPorCodigo((String) modelo.getValueAt(f, 1));
                    if (reg != null) { dlg.dispose(); abrirVentanaBoletaTracking(reg); }
                }
            }
        });
        raiz.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Pie: totales
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pie.setBackground(C_BLANCO);
        pie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDE));

        double totalImporte = lista.stream()
            .mapToDouble(r -> r.getEnvio().calcularCosto()).sum();
        JLabel lblTotal = new JLabel(String.format("Total acumulado:  S/. %.2f", totalImporte));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(accentColor != null ? accentColor : C_TEXTO);

        JLabel lblCant = new JLabel("Cantidad: " + lista.size() + " envio(s)");
        lblCant.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCant.setForeground(C_MUTED);

        JButton btnCerrar = crearBoton("Cerrar", C_ROJO, Color.WHITE);
        btnCerrar.addActionListener(ev -> dlg.dispose());

        pie.add(lblCant);
        pie.add(lblTotal);
        pie.add(btnCerrar);
        raiz.add(pie, BorderLayout.SOUTH);

        dlg.setContentPane(raiz);
        dlg.setVisible(true);
    }

    // -----------------------------------------------------------------------
    // PANEL: BUSQUEDA
    // -----------------------------------------------------------------------
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_FONDO);
        panel.add(crearEncabezadoPanel("Buscar Envio",
            "Busca por Codigo de Seguridad, DNI o Nombre del remitente / destinatario."),
            BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setBackground(C_FONDO);
        cuerpo.setBorder(new EmptyBorder(18, 32, 18, 32));

        // Barra de busqueda
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.setBackground(C_BLANCO);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDE, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        String[] criterios = {"Codigo de Seguridad", "DNI", "Nombre"};
        JComboBox<String> cboCriteria = new JComboBox<>(criterios);
        cboCriteria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboCriteria.setPreferredSize(new Dimension(200, 34));

        JTextField txtBusqueda = crearInput();
        txtBusqueda.setPreferredSize(new Dimension(270, 34));

        JButton btnBuscar = crearBoton("Buscar", C_ROJO, C_BLANCO);

        barra.add(new JLabel("Buscar por: "));
        barra.add(cboCriteria);
        barra.add(txtBusqueda);
        barra.add(btnBuscar);

        String[] cols = {"ID", "Codigo", "Remitente", "Destinatario", "Ruta", "Importe", "Estado", "Fecha"};
        modeloTablaResultados = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaRes = crearTabla(modeloTablaResultados);
        tablaRes.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int f = tablaRes.getSelectedRow();
                if (f != -1) {
                    RegistroEnvio reg = baseDatos.buscarPorCodigo(
                        (String) modeloTablaResultados.getValueAt(f, 1));
                    if (reg != null) abrirVentanaBoletaTracking(reg);
                }
            }
        });

        cuerpo.add(barra, BorderLayout.NORTH);
        cuerpo.add(new JScrollPane(tablaRes), BorderLayout.CENTER);
        panel.add(cuerpo, BorderLayout.CENTER);

        Runnable ejecutar = () -> {
            String termino  = txtBusqueda.getText().trim();
            String criterio = (String) cboCriteria.getSelectedItem();
            if (termino.isEmpty()) { mostrarError("Ingrese un termino de busqueda."); return; }
            modeloTablaResultados.setRowCount(0);

            List<RegistroEnvio> res;
            switch (criterio) {
                case "Codigo de Seguridad":
                    RegistroEnvio uno = baseDatos.buscarPorCodigo(termino);
                    res = new ArrayList<>();
                    if (uno != null) res.add(uno);
                    break;
                case "DNI":
                    res = baseDatos.buscarPorDni(termino);
                    break;
                default:
                    res = baseDatos.buscarPorNombre(termino);
            }

            if (res.isEmpty()) {
                JOptionPane.showMessageDialog(ventanaBase,
                    "No se encontraron envios para: \"" + termino + "\"",
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            } else {
                res.forEach(r -> modeloTablaResultados.addRow(filaDeRegistro(r)));
            }
        };

        btnBuscar.addActionListener(e -> ejecutar.run());
        txtBusqueda.addActionListener(e -> ejecutar.run());

        return panel;
    }

    // -----------------------------------------------------------------------
    // PANEL: ACTUALIZAR ESTADO
    // -----------------------------------------------------------------------
    private JPanel crearPanelActualizarEstado() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_FONDO);
        panel.add(crearEncabezadoPanel("Actualizar Estado de Envio",
            "Cambie el estado manualmente o avance al siguiente paso del flujo logistico."),
            BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setBackground(C_FONDO);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDE, 1),
            BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));
        card.setPreferredSize(new Dimension(500, 300));

        JTextField txtCodigo = crearInput();
        txtCodigo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        RegistroEnvio.EstadoEnvio[] estados = {
            RegistroEnvio.EstadoEnvio.PENDIENTE,
            RegistroEnvio.EstadoEnvio.EN_ALMACEN,
            RegistroEnvio.EstadoEnvio.EN_TRANSITO,
            RegistroEnvio.EstadoEnvio.ENTREGADO
        };
        JComboBox<RegistroEnvio.EstadoEnvio> cboEstado = new JComboBox<>(estados);
        cboEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnAplicar = crearBoton("Aplicar Estado Seleccionado",  C_VERDE, C_BLANCO);
        JButton btnAvanzar = crearBoton("Avanzar al Siguiente Estado",  new Color(240, 242, 245), C_TEXTO);
        btnAplicar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAvanzar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblResult = new JLabel(" ");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResult.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(crearLabelForm("Codigo de Seguridad:"));
        card.add(Box.createVerticalStrut(5)); card.add(txtCodigo);
        card.add(Box.createVerticalStrut(16));
        card.add(crearLabelForm("Nuevo Estado:"));
        card.add(Box.createVerticalStrut(5)); card.add(cboEstado);
        card.add(Box.createVerticalStrut(20));
        card.add(btnAplicar);
        card.add(Box.createVerticalStrut(8));
        card.add(btnAvanzar);
        card.add(Box.createVerticalStrut(16));
        card.add(lblResult);

        cuerpo.add(card);
        panel.add(cuerpo, BorderLayout.CENTER);

        btnAplicar.addActionListener(e -> {
            String cod = txtCodigo.getText().trim().toUpperCase();
            if (cod.isEmpty()) { lblResult.setForeground(C_ROJO); lblResult.setText("Ingrese el codigo."); return; }
            RegistroEnvio.EstadoEnvio st = (RegistroEnvio.EstadoEnvio) cboEstado.getSelectedItem();
            if (baseDatos.actualizarEstado(cod, st)) {
                lblResult.setForeground(C_VERDE);
                lblResult.setText("[OK] Estado actualizado: " + st);
                actualizarFilaEnHistorial(cod); refrescarEstadisticas();
            } else {
                lblResult.setForeground(C_ROJO);
                lblResult.setText("[Error] Codigo no encontrado o envio ya cerrado.");
            }
        });

        btnAvanzar.addActionListener(e -> {
            String cod = txtCodigo.getText().trim().toUpperCase();
            if (cod.isEmpty()) { lblResult.setForeground(C_ROJO); lblResult.setText("Ingrese el codigo."); return; }
            if (baseDatos.avanzarEstado(cod)) {
                RegistroEnvio r = baseDatos.buscarPorCodigo(cod);
                lblResult.setForeground(C_VERDE);
                lblResult.setText("[OK] Avanzado a: " + (r != null ? r.getEstado() : "?"));
                actualizarFilaEnHistorial(cod); refrescarEstadisticas();
            } else {
                lblResult.setForeground(C_ROJO);
                lblResult.setText("[Error] No se puede avanzar. Verifique el codigo o estado actual.");
            }
        });

        return panel;
    }

    // -----------------------------------------------------------------------
    // PANEL: CANCELAR ENVIO
    // -----------------------------------------------------------------------
    private JPanel crearPanelCancelarEnvio() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_FONDO);
        panel.add(crearEncabezadoPanel("Cancelar Envio",
            "Esta accion es irreversible. El envio quedara marcado como CANCELADO."),
            BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setBackground(C_FONDO);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, C_ROJO),
            BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));
        card.setPreferredSize(new Dimension(500, 260));

        JLabel lblAviso = new JLabel("(*) Un envio ya entregado no puede cancelarse.");
        lblAviso.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAviso.setForeground(C_AMARILLO);
        lblAviso.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtCodigo = crearInput();
        txtCodigo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnCancelar = crearBoton("Confirmar Cancelacion", C_ROJO, C_BLANCO);
        btnCancelar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblResult = new JLabel(" ");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResult.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblAviso);
        card.add(Box.createVerticalStrut(18));
        card.add(crearLabelForm("Codigo de Seguridad del Envio:"));
        card.add(Box.createVerticalStrut(5)); card.add(txtCodigo);
        card.add(Box.createVerticalStrut(18));
        card.add(btnCancelar);
        card.add(Box.createVerticalStrut(14));
        card.add(lblResult);

        cuerpo.add(card);
        panel.add(cuerpo, BorderLayout.CENTER);

        btnCancelar.addActionListener(e -> {
            String cod = txtCodigo.getText().trim().toUpperCase();
            if (cod.isEmpty()) { lblResult.setForeground(C_ROJO); lblResult.setText("Ingrese el codigo."); return; }

            int conf = JOptionPane.showConfirmDialog(ventanaBase,
                "Confirma la cancelacion del envio: " + cod + "?\nEsta accion no puede revertirse.",
                "Confirmar cancelacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf != JOptionPane.YES_OPTION) return;

            if (baseDatos.cancelarEnvio(cod)) {
                lblResult.setForeground(C_VERDE);
                lblResult.setText("[OK] Envio " + cod + " cancelado correctamente.");
                actualizarFilaEnHistorial(cod); refrescarEstadisticas(); txtCodigo.setText("");
            } else {
                lblResult.setForeground(C_ROJO);
                lblResult.setText("[Error] No encontrado, ya entregado o ya cancelado.");
            }
        });

        return panel;
    }

    // -----------------------------------------------------------------------
    // VENTANA: BOLETA + TRACKING
    // -----------------------------------------------------------------------
    private void abrirVentanaBoletaTracking(RegistroEnvio reg) {
        IEnvio envio = reg.getEnvio();

        JDialog dlg = new JDialog(ventanaBase, "Comprobante — " + reg.getCodigoSeguridad(), true);
        dlg.setSize(720, 600);
        dlg.setLocationRelativeTo(ventanaBase);

        JPanel raiz = new JPanel(new BorderLayout(0, 0));
        raiz.setBackground(C_FONDO);

        // Header
        JPanel headerBoleta = new JPanel(new BorderLayout());
        headerBoleta.setBackground(C_ROJO);
        headerBoleta.setBorder(new EmptyBorder(14, 22, 14, 22));
        JLabel lblHT = new JLabel("COMPROBANTE DE ENVIO  -  EL RAPIDITO EXPRESS");
        lblHT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHT.setForeground(Color.WHITE);
        JLabel lblCod = new JLabel("Cod: " + reg.getCodigoSeguridad());
        lblCod.setFont(new Font("Courier New", Font.BOLD, 13));
        lblCod.setForeground(new Color(255, 200, 200));
        headerBoleta.add(lblHT,  BorderLayout.WEST);
        headerBoleta.add(lblCod, BorderLayout.EAST);

        // Cuerpo: 2 columnas
        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 1, 0));
        cuerpo.setBackground(C_BORDE);

        // Columna datos
        JPanel colDatos = new JPanel();
        colDatos.setLayout(new BoxLayout(colDatos, BoxLayout.Y_AXIS));
        colDatos.setBackground(C_BLANCO);
        colDatos.setBorder(new EmptyBorder(18, 18, 18, 18));

        colDatos.add(crearLineaBoleta("Fecha",         reg.getFechaRegistro()));
        colDatos.add(crearLineaBoleta("Atendido por",  sesion.getNombreUsuario()));
        colDatos.add(Box.createVerticalStrut(8));
        colDatos.add(crearSeparadorBoleta());
        colDatos.add(Box.createVerticalStrut(8));
        colDatos.add(crearLineaBoleta("Remitente",     reg.getRemitente()));
        colDatos.add(crearLineaBoleta("DNI Rem.",      reg.getDniRemitente()));
        colDatos.add(crearLineaBoleta("Cel. Rem.",     reg.getCelRemitente()));
        colDatos.add(Box.createVerticalStrut(8));
        colDatos.add(crearLineaBoleta("Destinatario",  reg.getDestinatario()));
        colDatos.add(crearLineaBoleta("DNI Dest.",     reg.getDniDestinatario()));
        colDatos.add(crearLineaBoleta("Cel. Dest.",    reg.getCelDestinatario()));
        colDatos.add(Box.createVerticalStrut(8));
        colDatos.add(crearSeparadorBoleta());
        colDatos.add(Box.createVerticalStrut(8));
        colDatos.add(crearLineaBoleta("Origen",        reg.getAgenciaOrigen()));
        colDatos.add(crearLineaBoleta("Destino",       reg.getAgenciaDestino()));
        colDatos.add(crearLineaBoleta("Contenido",     envio.getDescripcion().replace("\n", " ")));
        colDatos.add(Box.createVerticalStrut(10));
        colDatos.add(crearSeparadorBoleta());
        colDatos.add(Box.createVerticalStrut(8));
        colDatos.add(crearLineaBoleta("Pago",           reg.getMetodoPago()));
        colDatos.add(Box.createVerticalStrut(4));

        JLabel lblTotal = new JLabel(String.format("TOTAL:  S/. %.2f", envio.calcularCosto()));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(C_ROJO);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        colDatos.add(lblTotal);

        // Tarifa info
        String tarifaInfo = CalculadorTarifa.getEtiquetaTarifa(reg.getAgenciaOrigen(), reg.getAgenciaDestino());
        JLabel lblTarifa = new JLabel(tarifaInfo);
        lblTarifa.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblTarifa.setForeground(C_MUTED);
        lblTarifa.setAlignmentX(Component.LEFT_ALIGNMENT);
        colDatos.add(Box.createVerticalStrut(4));
        colDatos.add(lblTarifa);

        colDatos.add(Box.createVerticalStrut(4));
        RegistroEnvio.EstadoEnvio estadoActual = reg.getEstado();
        Color colorEstado;
        switch (estadoActual) {
            case ENTREGADO:  colorEstado = C_VERDE;    break;
            case CANCELADO:  colorEstado = C_ROJO;     break;
            case EN_TRANSITO: colorEstado = new Color(59, 130, 246); break;
            default:         colorEstado = C_AMARILLO; break; // PENDIENTE / EN_ALMACEN
        }
        JLabel lblEst = new JLabel("Estado: " + reg.getEstado());
        lblEst.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEst.setForeground(colorEstado);
        lblEst.setAlignmentX(Component.LEFT_ALIGNMENT);
        colDatos.add(lblEst);

        // Columna tracking
        JPanel colTracking = new JPanel(new BorderLayout(0, 10));
        colTracking.setBackground(new Color(248, 250, 252));
        colTracking.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel lblTT = new JLabel("Seguimiento del Envio");
        lblTT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTT.setForeground(C_TEXTO);
        colTracking.add(lblTT, BorderLayout.NORTH);

        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        JList<String> listaTracking = new JList<>(modeloLista);
        listaTracking.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listaTracking.setFixedCellHeight(36);
        listaTracking.setBackground(C_BLANCO);
        listaTracking.setBorder(new EmptyBorder(6, 8, 6, 8));
        colTracking.add(new JScrollPane(listaTracking), BorderLayout.CENTER);

        cuerpo.add(colDatos);
        cuerpo.add(colTracking);

        raiz.add(headerBoleta, BorderLayout.NORTH);
        raiz.add(cuerpo,       BorderLayout.CENTER);
        dlg.setContentPane(raiz);

        // -------------------------------------------------------------------
        // PASOS DE TRACKING — 3 pasos (estados simplificados)
        // -------------------------------------------------------------------
        boolean express = envio.getDescripcion().contains("Express");
        RegistroEnvio.EstadoEnvio est = reg.getEstado();

        int nivel;
        switch (est) {
            case PENDIENTE:   nivel = 1; break;
            case EN_ALMACEN:  nivel = 2; break;
            case EN_TRANSITO: nivel = 3; break;
            case ENTREGADO:   nivel = 4; break;
            default:          nivel = 0; break; // CANCELADO
        }

        List<String> listaPasos = new ArrayList<>();

        if (nivel == 0) {
            listaPasos.add("[!] ENVIO CANCELADO");
            listaPasos.add("Cod: " + reg.getCodigoSeguridad());
            listaPasos.add("Este envio fue anulado por el operador.");
            listaPasos.add("No se realizara ninguna entrega.");
        } else {
            listaPasos.add("[1/3] Pedido registrado. En espera de recojo.");
            if (nivel >= 2)
                listaPasos.add("[2/3] Recibido y procesado en almacen central.");
            if (nivel >= 3)
                listaPasos.add("[3/3] En transito hacia " + reg.getAgenciaDestino() + ".");
            if (nivel == 4)
                listaPasos.add("[OK] Entregado exitosamente a " + reg.getDestinatario() + ".");
            else
                listaPasos.add("Tiempo estimado: " + (express ? "12 horas (Express)" : "24-48 horas (Normal)"));
        }

        String[] pasos = listaPasos.toArray(new String[0]);

        // Animar con colores diferenciados segun si fue cancelado o no
        Color colorPaso = (nivel == 0) ? C_ROJO : C_VERDE;
        listaTracking.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                if (!isSelected) {
                    lbl.setForeground(nivel == 0 ? C_ROJO : (index == pasos.length - 1 ? C_MUTED : C_TEXTO));
                    lbl.setBackground(index % 2 == 0 ? C_BLANCO : new Color(249, 250, 251));
                }
                return lbl;
            }
        });

        Timer timer = new Timer(700, new ActionListener() {
            int i = 0;
            @Override public void actionPerformed(ActionEvent e) {
                if (i < pasos.length) {
                    modeloLista.addElement(pasos[i]);
                    listaTracking.setSelectedIndex(i++);
                } else {
                    ((Timer) e.getSource()).stop();
                    listaTracking.clearSelection(); // quitar seleccion al terminar
                }
            }
        });
        timer.start();
        dlg.setVisible(true);
    }

    // -----------------------------------------------------------------------
    // HELPERS DE UI
    // -----------------------------------------------------------------------

    private JPanel crearEncabezadoPanel(String titulo, String subtitulo) {
        JPanel enc = new JPanel(new BorderLayout());
        enc.setBackground(C_BLANCO);
        enc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDE),
            BorderFactory.createEmptyBorder(18, 32, 18, 32)
        ));
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblT.setForeground(C_TEXTO);
        JLabel lblS = new JLabel(subtitulo);
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblS.setForeground(C_MUTED);
        enc.add(lblT, BorderLayout.NORTH);
        enc.add(lblS, BorderLayout.SOUTH);
        return enc;
    }

    private JLabel crearEncabezadoSeccion(String texto, Color color) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return lbl;
    }

    private JPanel crearSubHeader(String texto, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(color);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, color));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearColumnaPersona(String[] labels, JComponent[] campos, Color accent) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(C_BLANCO);
        col.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = crearLabelForm(labels[i]);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(lbl);
            col.add(Box.createVerticalStrut(3));
            JComponent c = campos[i];
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (c instanceof JTextField)
                ((JTextField) c).setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            col.add(c);
            if (i < labels.length - 1) col.add(Box.createVerticalStrut(10));
        }
        return col;
    }

    private JPanel crearFila2Col(String l1, JComponent c1, String l2, JComponent c2) {
        JPanel fila = new JPanel(new GridLayout(1, 2, 16, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        for (int i = 0; i < 2; i++) {
            String lbl = (i == 0) ? l1 : l2;
            JComponent c = (i == 0) ? c1 : c2;
            JPanel col = new JPanel();
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            col.setOpaque(false);
            JLabel label = crearLabelForm(lbl);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (c instanceof JTextField) ((JTextField)c).setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            if (c instanceof JComboBox)  ((JComboBox<?>)c).setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            col.add(label);
            col.add(Box.createVerticalStrut(3));
            col.add(c);
            fila.add(col);
        }
        return fila;
    }

    private JPanel crearFilaCheckboxes(JCheckBox cb1, JCheckBox cb2) {
        JPanel fila = new JPanel(new GridLayout(1, 2, 16, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p1.setOpaque(false); p1.add(cb1);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p2.setOpaque(false); p2.add(cb2);
        fila.add(p1); fila.add(p2);
        return fila;
    }

    private JTextField crearInput() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(C_INPUT_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDE, 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }

    private JComboBox<String> crearCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(C_INPUT_BG);
        return c;
    }

    private JLabel crearLabelForm(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(55, 65, 81));
        return lbl;
    }

    private JButton crearBoton(String texto, Color fondo, Color textoColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(textoColor);
        btn.setBackground(fondo);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void estilizarCheckbox(JCheckBox cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setForeground(C_TEXTO);
        cb.setOpaque(false);
    }

    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(229, 231, 235));
        tabla.setSelectionBackground(new Color(219, 234, 254));
        tabla.setSelectionForeground(C_TEXTO);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(249, 250, 251));
        tabla.getTableHeader().setForeground(C_MUTED);
        tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDE));
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) comp.setBackground(r % 2 == 0 ? C_BLANCO : new Color(249, 250, 251));
                return comp;
            }
        });
        return tabla;
    }

    private JPanel crearLineaBoleta(String clave, String valor) {
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblC = new JLabel(clave + ":");
        lblC.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblC.setForeground(C_MUTED);
        lblC.setPreferredSize(new Dimension(95, 18));
        JLabel lblV = new JLabel(valor == null || valor.isEmpty() ? "—" : valor);
        lblV.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblV.setForeground(C_TEXTO);
        fila.add(lblC, BorderLayout.WEST);
        fila.add(lblV, BorderLayout.CENTER);
        return fila;
    }

    private JSeparator crearSeparadorBoleta() {
        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(ventanaBase, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private Object[] filaDeRegistro(RegistroEnvio r) {
        return new Object[]{
            r.getIdInterno(), r.getCodigoSeguridad(),
            r.getRemitente(), r.getDestinatario(),
            r.getAgenciaOrigen() + " > " + r.getAgenciaDestino(),
            String.format("S/. %.2f", r.getEnvio().calcularCosto()),
            r.getEstado().toString(),
            r.getFechaRegistro()
        };
    }

    private void actualizarFilaEnHistorial(String codigo) {
        for (int i = 0; i < modeloTablaHistorial.getRowCount(); i++) {
            if (codigo.equals(modeloTablaHistorial.getValueAt(i, 1))) {
                RegistroEnvio reg = baseDatos.buscarPorCodigo(codigo);
                if (reg != null) modeloTablaHistorial.setValueAt(reg.getEstado().toString(), i, 6);
                break;
            }
        }
    }

    // -----------------------------------------------------------------------
    // ACTION LISTENER (sidebar)
    // -----------------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "PANEL_INICIO":    refrescarEstadisticas();
                                    navegadorPaneles.show(workspaceDerecho, "INICIO");    break;
            case "PANEL_NUEVO":     navegadorPaneles.show(workspaceDerecho, "NUEVO");     break;
            case "PANEL_HISTORIAL": navegadorPaneles.show(workspaceDerecho, "HISTORIAL"); break;
            case "PANEL_BUSQUEDA":  navegadorPaneles.show(workspaceDerecho, "BUSQUEDA"); break;
            case "PANEL_ESTADO":    navegadorPaneles.show(workspaceDerecho, "ESTADO");   break;
            case "PANEL_CANCELAR":  navegadorPaneles.show(workspaceDerecho, "CANCELAR"); break;
            case "CERRAR_SESION":
                sesion.cerrarSesion();
                ventanaBase.dispose();
                System.exit(0);
                break;
        }
    }
}