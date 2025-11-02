/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aplikasipengelolakontak;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;
import java.util.Vector;

public class AplikasiPengelolaKontak extends JFrame {
    // Komponen GUI
    private JTextField txtNama, txtTelepon, txtCari;
    private JComboBox<String> cmbKategori;
    private JTable tabelKontak;
    private DefaultTableModel modelTabel;
    private JButton btnTambah, btnEdit, btnHapus, btnCari, btnEkspor, btnImpor, btnBersih;
    
    // Database
    private Connection conn;
    private int selectedId = -1;
    
    public AplikasiPengelolaKontak() {
        if (!initDatabase()) {
            JOptionPane.showMessageDialog(null, 
                "Gagal menghubungkan ke database!\n" +
                "Pastikan SQLite JDBC driver sudah ditambahkan ke Libraries.\n" +
                "Download dari: https://github.com/xerial/sqlite-jdbc/releases",
                "Error Database",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        initComponents();
        loadData();
    }
    
    private boolean initDatabase() {
        try {
            // Load SQLite JDBC Driver
            Class.forName("org.sqlite.JDBC");
            
            // PILIHAN 1: SQLite (Database File)
            String url = "jdbc:sqlite:kontak.db";
            conn = DriverManager.getConnection(url);
            
            System.out.println("Database connected successfully!");
            
            String sql = "CREATE TABLE IF NOT EXISTS kontak (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nama TEXT NOT NULL," +
                        "telepon TEXT NOT NULL," +
                        "kategori TEXT NOT NULL)";
            
            /* PILIHAN 2: MySQL (Uncomment untuk menggunakan MySQL)
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/db_kontak";
            String user = "root";
            String password = ""; // Ganti dengan password MySQL Anda
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "CREATE TABLE IF NOT EXISTS kontak (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "nama VARCHAR(100) NOT NULL," +
                        "telepon VARCHAR(15) NOT NULL," +
                        "kategori VARCHAR(50) NOT NULL)";
            */
            
            /* PILIHAN 3: PostgreSQL (Uncomment untuk menggunakan PostgreSQL)
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/db_kontak";
            String user = "postgres";
            String password = "password"; // Ganti dengan password PostgreSQL Anda
            conn = DriverManager.getConnection(url, user, password);
            
            String sql = "CREATE TABLE IF NOT EXISTS kontak (" +
                        "id SERIAL PRIMARY KEY," +
                        "nama VARCHAR(100) NOT NULL," +
                        "telepon VARCHAR(15) NOT NULL," +
                        "kategori VARCHAR(50) NOT NULL)";
            */
            
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver tidak ditemukan!");
            System.err.println("Download SQLite JDBC dari: https://github.com/xerial/sqlite-jdbc/releases");
            System.err.println("Lalu tambahkan ke Libraries di NetBeans");
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            System.err.println("Error koneksi database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void initComponents() {
        setTitle("Aplikasi Pengelolaan Kontak");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Panel Input
        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBorder(BorderFactory.createTitledBorder("Informasi Kontak"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nama
        gbc.gridx = 0; gbc.gridy = 0;
        panelInput.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNama = new JTextField(20);
        panelInput.add(txtNama, gbc);
        
        // Telepon
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelInput.add(new JLabel("Telepon:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTelepon = new JTextField(20);
        panelInput.add(txtTelepon, gbc);
        
        // Kategori
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelInput.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        String[] kategori = {"Keluarga", "Teman", "Kerja"};
        cmbKategori = new JComboBox<>(kategori);
        panelInput.add(cmbKategori, gbc);
        
        // Panel Tombol Aksi
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnTambah = new JButton("Tambah");
        btnEdit = new JButton("Edit");
        btnHapus = new JButton("Hapus");
        btnBersih = new JButton("Bersihkan");
        
        btnTambah.setBackground(new Color(46, 204, 113));
        btnTambah.setForeground(Color.WHITE);
        btnEdit.setBackground(new Color(52, 152, 219));
        btnEdit.setForeground(Color.WHITE);
        btnHapus.setBackground(new Color(231, 76, 60));
        btnHapus.setForeground(Color.WHITE);
        
        panelTombol.add(btnTambah);
        panelTombol.add(btnEdit);
        panelTombol.add(btnHapus);
        panelTombol.add(btnBersih);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panelInput.add(panelTombol, gbc);
        
        // Panel Pencarian
        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCari.setBorder(BorderFactory.createTitledBorder("Pencarian"));
        panelCari.add(new JLabel("Cari:"));
        txtCari = new JTextField(20);
        btnCari = new JButton("Cari");
        btnCari.setBackground(new Color(241, 196, 15));
        panelCari.add(txtCari);
        panelCari.add(btnCari);
        
        // Panel File Operations
        JPanel panelFile = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEkspor = new JButton("Ekspor ke CSV");
        btnImpor = new JButton("Impor dari CSV");
        btnEkspor.setBackground(new Color(155, 89, 182));
        btnEkspor.setForeground(Color.WHITE);
        btnImpor.setBackground(new Color(52, 73, 94));
        btnImpor.setForeground(Color.WHITE);
        panelFile.add(btnEkspor);
        panelFile.add(btnImpor);
        
        // Panel Atas
        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.add(panelInput, BorderLayout.NORTH);
        JPanel panelBawahInput = new JPanel(new BorderLayout());
        panelBawahInput.add(panelCari, BorderLayout.WEST);
        panelBawahInput.add(panelFile, BorderLayout.EAST);
        panelAtas.add(panelBawahInput, BorderLayout.SOUTH);
        
        // Tabel
        String[] kolom = {"ID", "Nama", "Telepon", "Kategori"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelKontak = new JTable(modelTabel);
        tabelKontak.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelKontak.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelKontak.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabelKontak.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabelKontak.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(tabelKontak);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Kontak"));
        
        // Tambahkan ke Frame
        add(panelAtas, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Event Listeners
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        // Tombol Tambah
        btnTambah.addActionListener(e -> tambahKontak());
        
        // Tombol Edit
        btnEdit.addActionListener(e -> editKontak());
        
        // Tombol Hapus
        btnHapus.addActionListener(e -> hapusKontak());
        
        // Tombol Cari
        btnCari.addActionListener(e -> cariKontak());
        
        // Tombol Bersih
        btnBersih.addActionListener(e -> bersihkanForm());
        
        // Tombol Ekspor
        btnEkspor.addActionListener(e -> eksporKeCSV());
        
        // Tombol Impor
        btnImpor.addActionListener(e -> imporDariCSV());
        
        // Klik pada tabel
        tabelKontak.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabelKontak.getSelectedRow();
                if (row != -1) {
                    selectedId = (int) modelTabel.getValueAt(row, 0);
                    txtNama.setText(modelTabel.getValueAt(row, 1).toString());
                    txtTelepon.setText(modelTabel.getValueAt(row, 2).toString());
                    cmbKategori.setSelectedItem(modelTabel.getValueAt(row, 3).toString());
                }
            }
        });
        
        // ItemListener untuk ComboBox
        cmbKategori.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // Bisa ditambahkan logika tambahan saat kategori berubah
            }
        });
        
        // Enter pada text field cari
        txtCari.addActionListener(e -> cariKontak());
    }
    
    private boolean validasiInput() {
        if (txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama harus diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtNama.requestFocus();
            return false;
        }
        
        String telepon = txtTelepon.getText().trim();
        if (telepon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor telepon harus diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtTelepon.requestFocus();
            return false;
        }
        
        // Validasi telepon hanya angka
        if (!telepon.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Nomor telepon hanya boleh berisi angka!", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtTelepon.requestFocus();
            return false;
        }
        
        // Validasi panjang telepon
        if (telepon.length() < 10 || telepon.length() > 15) {
            JOptionPane.showMessageDialog(this, "Nomor telepon harus 10-15 digit!", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtTelepon.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void tambahKontak() {
        if (!validasiInput()) return;
        
        try {
            String sql = "INSERT INTO kontak (nama, telepon, kategori) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtNama.getText().trim());
            pstmt.setString(2, txtTelepon.getText().trim());
            pstmt.setString(3, cmbKategori.getSelectedItem().toString());
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, "Kontak berhasil ditambahkan!");
            bersihkanForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void editKontak() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kontak yang akan diedit!");
            return;
        }
        
        if (!validasiInput()) return;
        
        try {
            String sql = "UPDATE kontak SET nama=?, telepon=?, kategori=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtNama.getText().trim());
            pstmt.setString(2, txtTelepon.getText().trim());
            pstmt.setString(3, cmbKategori.getSelectedItem().toString());
            pstmt.setInt(4, selectedId);
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, "Kontak berhasil diupdate!");
            bersihkanForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void hapusKontak() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kontak yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus kontak ini?", 
            "Konfirmasi", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM kontak WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, selectedId);
                pstmt.executeUpdate();
                pstmt.close();
                
                JOptionPane.showMessageDialog(this, "Kontak berhasil dihapus!");
                bersihkanForm();
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void cariKontak() {
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        
        try {
            modelTabel.setRowCount(0);
            String sql = "SELECT * FROM kontak WHERE nama LIKE ? OR telepon LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("telepon"),
                    rs.getString("kategori")
                };
                modelTabel.addRow(row);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void loadData() {
        if (conn == null) {
            System.err.println("Koneksi database null!");
            return;
        }
        
        try {
            modelTabel.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM kontak ORDER BY nama");
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("telepon"),
                    rs.getString("kategori")
                };
                modelTabel.addRow(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error memuat data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void bersihkanForm() {
        txtNama.setText("");
        txtTelepon.setText("");
        txtCari.setText("");
        cmbKategori.setSelectedIndex(0);
        selectedId = -1;
        tabelKontak.clearSelection();
    }
    
    private void eksporKeCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File CSV");
        fileChooser.setSelectedFile(new File("kontak.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Nama,Telepon,Kategori");
                
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT nama, telepon, kategori FROM kontak");
                
                while (rs.next()) {
                    writer.printf("%s,%s,%s%n",
                        rs.getString("nama"),
                        rs.getString("telepon"),
                        rs.getString("kategori"));
                }
                rs.close();
                stmt.close();
                
                JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke CSV!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error ekspor: " + e.getMessage());
            }
        }
    }
    
    private void imporDariCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih File CSV");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            int count = 0;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine(); // Skip header
                
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO kontak (nama, telepon, kategori) VALUES (?, ?, ?)");
                
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 3) {
                        pstmt.setString(1, data[0].trim());
                        pstmt.setString(2, data[1].trim());
                        pstmt.setString(3, data[2].trim());
                        pstmt.executeUpdate();
                        count++;
                    }
                }
                pstmt.close();
                
                JOptionPane.showMessageDialog(this, 
                    count + " kontak berhasil diimpor!");
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error impor: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            AplikasiPengelolaKontak app = new AplikasiPengelolaKontak();
            app.setVisible(true);
        });
    }
}
