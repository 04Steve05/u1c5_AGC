package controlador;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.InputEvent;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import vista.ventana;
import modelo.*;

//Definición de la clase logica_ventana que implementa tres interfaces para manejar eventos.
public class logica_ventana implements ActionListener, ListSelectionListener, ItemListener {
	private ventana delegado; // Referencia a la ventana principal que contiene la GUI.
	private String nombres, email, telefono, categoria=""; // Variables para almacenar datos del contacto.
	private persona persona; // Objeto de tipo persona, que representa un contacto.
	private List<persona> contactos; // Lista de objetos persona que representa todos los contactos.
	private boolean favorito = false; // Booleano que indica si un contacto es favorito.
	private JPopupMenu menuContextual;

	// Constructor que inicializa la clase y configura los escuchadores de eventos para los componentes de la GUI.
	public logica_ventana(ventana delegado) {
		  // Asigna la ventana recibida como parámetro a la variable de instancia delegado.
	    this.delegado = delegado;
	    // Carga los contactos almacenados al inicializar.
	    cargarContactosRegistrados(); 
	    // Registra los ActionListener para los botones de la GUI.
	    this.delegado.btn_add.addActionListener(this);
	    this.delegado.btn_eliminar.addActionListener(this);
	    this.delegado.btn_modificar.addActionListener(this);
	    this.delegado.btn_exportar.addActionListener(this);
	    // Registra los ListSelectionListener para la lista de contactos.
	    this.delegado.lst_contactos.addListSelectionListener(this);
	    // Registra los ItemListener para el JComboBox de categoría y el JCheckBox de favoritos.
	    this.delegado.cmb_categoria.addItemListener(this);
	    this.delegado.chb_favorito.addItemListener(this);

	    this.delegado.txt_buscar.addKeyListener(new KeyAdapter() {
	        public void keyReleased(KeyEvent e) {
	            buscarContacto();
	        }
	    });

	    this.delegado.tabla_contactos.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            if (e.getClickCount() == 2) {
	                cargarContactoDesdeTabla();
	            }
	        }
	    });

	    configurarAtajosTeclado();
	    configurarMenuContextual();
	}

	// Método privado para inicializar las variables con los valores ingresados en la GUI.
	private void incializacionCampos() {
		// Obtiene el texto ingresado en los campos de nombres, email y teléfono de la GUI.
		nombres = delegado.txt_nombres.getText();
		email = delegado.txt_email.getText();
		telefono = delegado.txt_telefono.getText();
	}

	// Método privado para cargar los contactos almacenados desde un archivo.
	private void cargarContactosRegistrados() {
		 try {
		        delegado.barraProgreso.setVisible(true);
		        delegado.barraProgreso.setValue(0);

		        // Lee los contactos almacenados utilizando una instancia de personaDAO.
		        contactos = new personaDAO(new persona()).leerArchivo();
		        DefaultListModel modelo = new DefaultListModel();
		        // Agrega cada contacto al modelo de la lista de contactos de la GUI.
		        for (persona contacto : contactos) {
		            modelo.addElement(contacto.formatoLista());
		        }
		        // Establece el modelo actualizado en la lista de contactos de la GUI.
		        delegado.lst_contactos.setModel(modelo);
		        delegado.barraProgreso.setValue(50);

		        cargarTabla();
		        delegado.barraProgreso.setValue(75);

		        actualizarEstadisticas();
		        delegado.barraProgreso.setValue(100);

		        delegado.barraProgreso.setVisible(false);
		    } catch (IOException e) {
		        // Muestra un mensaje de error si ocurre una excepción al cargar los contactos.
		        JOptionPane.showMessageDialog(delegado, "Existen problemas al cargar todos los contactos");
		        delegado.barraProgreso.setVisible(false);
		    }
	}

	// Método privado para limpiar los campos de entrada en la GUI y reiniciar variables.
	private void limpiarCampos() {
		// Limpia los campos de nombres, email y teléfono en la GUI.
	    delegado.txt_nombres.setText("");
	    delegado.txt_telefono.setText("");
	    delegado.txt_email.setText("");
	    // Reinicia las variables de categoría y favorito.
	    categoria = "";
	    favorito = false;
	    // Desmarca la casilla de favorito y establece la categoría por defecto.
	    delegado.chb_favorito.setSelected(favorito);
	    delegado.cmb_categoria.setSelectedIndex(0);
	    // Reinicia las variables con los valores actuales de la GUI.
	    incializacionCampos();
	    // Recarga los contactos en la lista de contactos de la GUI.
	    cargarContactosRegistrados();
	}

	// Método que maneja los eventos de acción (clic) en los botones.
	@Override
	public void actionPerformed(ActionEvent e) {
		incializacionCampos(); // Inicializa las variables con los valores actuales de la GUI.

	    // Verifica si el evento proviene del botón "Agregar".
	    if (e.getSource() == delegado.btn_add) {
	        // Verifica si los campos de nombres, teléfono y email no están vacíos.
	        if ((!nombres.equals("")) && (!telefono.equals("")) && (!email.equals(""))) {
	            // Verifica si se ha seleccionado una categoría válida.
	            if ((!categoria.equals("Elija una Categoria")) && (!categoria.equals(""))) {
	                // Crea un nuevo objeto persona con los datos ingresados y lo guarda.
	                persona = new persona(nombres, telefono, email, categoria, favorito);
	                new personaDAO(persona).escribirArchivo();
	                // Limpia los campos después de agregar el contacto.
	                limpiarCampos();
	                // Muestra un mensaje de éxito.
	                JOptionPane.showMessageDialog(delegado, "Contacto Registrado!!!");
	            } else {
	                // Muestra un mensaje de advertencia si no se ha seleccionado una categoría válida.
	                JOptionPane.showMessageDialog(delegado, "Elija una Categoria!!!");
	            }
	        } else {
	            // Muestra un mensaje de advertencia si algún campo está vacío.
	            JOptionPane.showMessageDialog(delegado, "Todos los campos deben ser llenados!!!");
	        }
	    } else if (e.getSource() == delegado.btn_eliminar) {
	        eliminarContacto();
	    } else if (e.getSource() == delegado.btn_modificar) {
	        modificarContacto();
	    } else if (e.getSource() == delegado.btn_exportar) {
	        exportarCSV();
	    }
	}

	// Método que maneja los eventos de selección en la lista de contactos.
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// Obtiene el índice del elemento seleccionado en la lista de contactos.
	    int index = delegado.lst_contactos.getSelectedIndex();
	    // Verifica si se ha seleccionado un índice válido en la lista.
	    if (index != -1) {
	        // Si el índice es mayor que cero (no se seleccionó la primera fila),
	        // carga los detalles del contacto seleccionado.
	        if (index > 0) {
	            cargarContacto(index);
	        }
	    } 
	}

	// Método privado para cargar los datos del contacto seleccionado en los campos de la GUI.
	private void cargarContacto(int index) {
		// Establece el nombre del contacto en el campo de texto de nombres.
	    delegado.txt_nombres.setText(contactos.get(index).getNombre());
	    // Establece el teléfono del contacto en el campo de texto de teléfono.
	    delegado.txt_telefono.setText(contactos.get(index).getTelefono());
	    // Establece el correo electrónico del contacto en el campo de texto de correo electrónico.
	    delegado.txt_email.setText(contactos.get(index).getEmail());
	    // Establece el estado de favorito del contacto en el JCheckBox de favorito.
	    delegado.chb_favorito.setSelected(contactos.get(index).isFavorito());
	    // Establece la categoría del contacto en el JComboBox de categoría.
	    delegado.cmb_categoria.setSelectedItem(contactos.get(index).getCategoria());
	}

	// Método que maneja los eventos de cambio de estado en los componentes cmb_categoria y chb_favorito.
	@Override
	public void itemStateChanged(ItemEvent e) {
		// Verifica si el evento proviene del JComboBox de categoría.
	    if (e.getSource() == delegado.cmb_categoria) {
	        // Obtiene el elemento seleccionado en el JComboBox y lo convierte en una cadena.
	        categoria = delegado.cmb_categoria.getSelectedItem().toString();
	        // Actualiza la categoría seleccionada en la variable "categoria".
	    } else if (e.getSource() == delegado.chb_favorito) {
	        // Verifica si el evento proviene del JCheckBox de favorito.
	        favorito = delegado.chb_favorito.isSelected();
	        // Obtiene el estado seleccionado del JCheckBox y actualiza el estado de favorito en la variable "favorito".
	    }
	}

	private void cargarTabla() {
	    delegado.modeloTabla.setRowCount(0);
	    for (persona p : contactos) {
	        if (!p.getNombre().equals("NOMBRE")) {
	            Object[] fila = {
	                p.getNombre(),
	                p.getTelefono(),
	                p.getEmail(),
	                p.getCategoria(),
	                p.isFavorito() ? "Sí" : "No"
	            };
	            delegado.modeloTabla.addRow(fila);
	        }
	    }
	}

	private void actualizarEstadisticas() {
	    int total = 0;
	    int favoritos = 0;
	    int familia = 0;
	    int amigos = 0;
	    int trabajo = 0;

	    for (persona p : contactos) {
	        if (!p.getNombre().equals("NOMBRE")) {
	            total++;
	            if (p.isFavorito()) favoritos++;
	            if (p.getCategoria().equals("Familia")) familia++;
	            if (p.getCategoria().equals("Amigos")) amigos++;
	            if (p.getCategoria().equals("Trabajo")) trabajo++;
	        }
	    }

	    delegado.lbl_total_contactos.setText(String.valueOf(total));
	    delegado.lbl_total_favoritos.setText(String.valueOf(favoritos));
	    delegado.lbl_total_familia.setText("Familia: " + familia);
	    delegado.lbl_total_amigos.setText("Amigos: " + amigos);
	    delegado.lbl_total_trabajo.setText("Trabajo: " + trabajo);
	}

	private void buscarContacto() {
	    String busqueda = delegado.txt_buscar.getText().toLowerCase();
	    delegado.modeloTabla.setRowCount(0);

	    for (persona p : contactos) {
	        if (!p.getNombre().equals("NOMBRE")) {
	            if (p.getNombre().toLowerCase().contains(busqueda) ||
	                p.getTelefono().contains(busqueda) ||
	                p.getEmail().toLowerCase().contains(busqueda)) {
	                Object[] fila = {
	                    p.getNombre(),
	                    p.getTelefono(),
	                    p.getEmail(),
	                    p.getCategoria(),
	                    p.isFavorito() ? "Sí" : "No"
	                };
	                delegado.modeloTabla.addRow(fila);
	            }
	        }
	    }
	}

	private void cargarContactoDesdeTabla() {
	    int fila = delegado.tabla_contactos.getSelectedRow();
	    if (fila >= 0) {
	        delegado.txt_nombres.setText(delegado.tabla_contactos.getValueAt(fila, 0).toString());
	        delegado.txt_telefono.setText(delegado.tabla_contactos.getValueAt(fila, 1).toString());
	        delegado.txt_email.setText(delegado.tabla_contactos.getValueAt(fila, 2).toString());
	        delegado.cmb_categoria.setSelectedItem(delegado.tabla_contactos.getValueAt(fila, 3).toString());
	        delegado.chb_favorito.setSelected(delegado.tabla_contactos.getValueAt(fila, 4).toString().equals("Sí"));
	    }
	}

	private void exportarCSV() {
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("Guardar archivo CSV");
	    int resultado = fileChooser.showSaveDialog(delegado);

	    if (resultado == JFileChooser.APPROVE_OPTION) {
	        String ruta = fileChooser.getSelectedFile().getAbsolutePath();
	        if (!ruta.endsWith(".csv")) {
	            ruta += ".csv";
	        }

	        try {
	            java.io.FileWriter fw = new java.io.FileWriter(ruta);
	            fw.write("Nombre,Teléfono,Email,Categoría,Favorito\n");
	            for (persona p : contactos) {
	                if (!p.getNombre().equals("NOMBRE")) {
	                    fw.write(p.datosContacto().replace(";", ",") + "\n");
	                }
	            }
	            fw.close();
	            JOptionPane.showMessageDialog(delegado, "Archivo exportado correctamente");
	        } catch (Exception ex) {
	            JOptionPane.showMessageDialog(delegado, "Error al exportar: " + ex.getMessage());
	        }
	    }
	}

	private void configurarAtajosTeclado() {
	    // Ctrl+N para limpiar campos
	    Action accionNuevo = new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	            limpiarCampos();
	        }
	    };
	    delegado.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
	        .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "nuevo");
	    delegado.getRootPane().getActionMap().put("nuevo", accionNuevo);

	    // Ctrl+S para agregar contacto
	    Action accionGuardar = new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	            delegado.btn_add.doClick();
	        }
	    };
	    delegado.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
	        .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "guardar");
	    delegado.getRootPane().getActionMap().put("guardar", accionGuardar);

	    // Ctrl+E para exportar CSV
	    Action accionExportar = new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	            exportarCSV();
	        }
	    };
	    delegado.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
	        .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "exportar");
	    delegado.getRootPane().getActionMap().put("exportar", accionExportar);

	    // Ctrl+M para modificar
	    Action accionModificar = new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	            delegado.btn_modificar.doClick();
	        }
	    };
	    delegado.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
	        .put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), "modificar");
	    delegado.getRootPane().getActionMap().put("modificar", accionModificar);

	    // Delete para eliminar
	    Action accionEliminar = new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	            delegado.btn_eliminar.doClick();
	        }
	    };
	    delegado.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
	        .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
	    delegado.getRootPane().getActionMap().put("eliminar", accionEliminar);
	}

	private void configurarMenuContextual() {
	    menuContextual = new JPopupMenu();

	    JMenuItem itemCargar = new JMenuItem("Cargar contacto");
	    itemCargar.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            cargarContactoDesdeTabla();
	        }
	    });
	    menuContextual.add(itemCargar);

	    JMenuItem itemEliminar = new JMenuItem("Eliminar");
	    itemEliminar.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            delegado.btn_eliminar.doClick();
	        }
	    });
	    menuContextual.add(itemEliminar);

	    menuContextual.addSeparator();

	    JMenuItem itemExportar = new JMenuItem("Exportar a CSV");
	    itemExportar.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            exportarCSV();
	        }
	    });
	    menuContextual.add(itemExportar);

	    JMenuItem itemLimpiar = new JMenuItem("Limpiar campos");
	    itemLimpiar.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            limpiarCampos();
	        }
	    });
	    menuContextual.add(itemLimpiar);

	    delegado.tabla_contactos.addMouseListener(new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	            if (e.isPopupTrigger()) {
	                mostrarMenu(e);
	            }
	        }
	        public void mouseReleased(MouseEvent e) {
	            if (e.isPopupTrigger()) {
	                mostrarMenu(e);
	            }
	        }
	        private void mostrarMenu(MouseEvent e) {
	            int fila = delegado.tabla_contactos.rowAtPoint(e.getPoint());
	            if (fila >= 0) {
	                delegado.tabla_contactos.setRowSelectionInterval(fila, fila);
	            }
	            menuContextual.show(e.getComponent(), e.getX(), e.getY());
	        }
	    });
	}

	private void eliminarContacto() {
	    int fila = delegado.tabla_contactos.getSelectedRow();
	    if (fila >= 0) {
	        int confirmacion = JOptionPane.showConfirmDialog(delegado,
	            "¿Está seguro de eliminar este contacto?",
	            "Confirmar eliminación",
	            JOptionPane.YES_NO_OPTION);

	        if (confirmacion == JOptionPane.YES_OPTION) {
	            String nombre = delegado.tabla_contactos.getValueAt(fila, 0).toString();

	            for (int i = 0; i < contactos.size(); i++) {
	                if (contactos.get(i).getNombre().equals(nombre)) {
	                    contactos.remove(i);
	                    break;
	                }
	            }

	            try {
	                new personaDAO(new persona()).actualizarContactos(contactos);
	                limpiarCampos();
	                JOptionPane.showMessageDialog(delegado, "Contacto eliminado correctamente");
	            } catch (IOException ex) {
	                JOptionPane.showMessageDialog(delegado, "Error al eliminar el contacto: " + ex.getMessage());
	            }
	        }
	    } else {
	        JOptionPane.showMessageDialog(delegado, "Seleccione un contacto de la tabla para eliminar");
	    }
	}

	private void modificarContacto() {
	    int fila = delegado.tabla_contactos.getSelectedRow();
	    if (fila >= 0) {
	        incializacionCampos();

	        if ((!nombres.equals("")) && (!telefono.equals("")) && (!email.equals(""))) {
	            if ((!categoria.equals("Elija una Categoria")) && (!categoria.equals(""))) {
	                String nombreOriginal = delegado.tabla_contactos.getValueAt(fila, 0).toString();

	                for (int i = 0; i < contactos.size(); i++) {
	                    if (contactos.get(i).getNombre().equals(nombreOriginal)) {
	                        contactos.get(i).setNombre(nombres);
	                        contactos.get(i).setTelefono(telefono);
	                        contactos.get(i).setEmail(email);
	                        contactos.get(i).setCategoria(categoria);
	                        contactos.get(i).setFavorito(favorito);
	                        break;
	                    }
	                }

	                try {
	                    new personaDAO(new persona()).actualizarContactos(contactos);
	                    limpiarCampos();
	                    JOptionPane.showMessageDialog(delegado, "Contacto modificado correctamente");
	                } catch (IOException ex) {
	                    JOptionPane.showMessageDialog(delegado, "Error al modificar el contacto: " + ex.getMessage());
	                }
	            } else {
	                JOptionPane.showMessageDialog(delegado, "Elija una Categoria!!!");
	            }
	        } else {
	            JOptionPane.showMessageDialog(delegado, "Todos los campos deben ser llenados!!!");
	        }
	    } else {
	        JOptionPane.showMessageDialog(delegado, "Seleccione un contacto de la tabla para modificar");
	    }
	}
}