import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;

// Class Menu yang merupakan turunan dari JFrame
public class Menu extends JFrame {
    // Komponen GUI yang digunakan
    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JLabel kelasLabel;
    private JComboBox kelasComboBox;

    // Variabel untuk menyimpan indeks baris yang dipilih pada tabel
    private int selectedIndex = -1;
    // ArrayList untuk menyimpan data mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;
    // Objek untuk mengakses database
    private Database database;

    // Metode main untuk menjalankan program
    public static void main(String[] args) {
        // Membuat objek Menu
        Menu window = new Menu();
        // Mengatur ukuran jendela
        window.setSize(480, 560);
        // Mengatur jendela agar tampil di tengah layar
        window.setLocationRelativeTo(null);
        // Mengatur panel utama sebagai konten jendela
        window.setContentPane(window.mainPanel);
        // Mengatur warna latar belakang
        window.getContentPane().setBackground(Color.white);
        // Menampilkan jendela
        window.setVisible(true);
        // Mengatur operasi standar ketika menutup jendela
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Konstruktor Menu
    public Menu() {
        // Inisialisasi ArrayList untuk menyimpan data mahasiswa
        listMahasiswa = new ArrayList<>();
        // Inisialisasi objek Database
        database = new Database();
        // Mengatur model tabel
        mahasiswaTable.setModel(setTable());
        // Mengatur font untuk judul
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // Mengatur pilihan untuk combo box jenis kelamin
        String[] jenisKelaminData = {"", "Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jenisKelaminData));

        // Mengatur pilihan untuk combo box kelas
        String[] kelasData = {"", "A", "B", "C1", "C2"};
        kelasComboBox.setModel(new DefaultComboBoxModel(kelasData));

        // Mengatur agar tombol hapus tidak terlihat awalnya
        deleteButton.setVisible(false);

        // Menambahkan aksi untuk tombol Tambah/Perbarui
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isInputValid()) {
                    if (selectedIndex == -1) {
                        insertData();
                    } else {
                        updateData();
                    }
                }
            }
        });

        // Menambahkan aksi untuk tombol Hapus
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex >= 0) {
                    deleteData();
                }
            }
        });

        // Menambahkan aksi untuk tombol Batal
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // Menambahkan aksi ketika tabel mahasiswa di-klik
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Mendapatkan baris yang dipilih
                selectedIndex = mahasiswaTable.getSelectedRow();
                // Mendapatkan data dari baris yang dipilih
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();
                String selectedKelas = mahasiswaTable.getModel().getValueAt(selectedIndex, 4).toString();

                // Mengisi field input dengan data yang dipilih
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);
                kelasComboBox.setSelectedItem(selectedKelas);

                // Menonaktifkan field NIM karena tidak bisa diubah
                nimField.setEnabled(false);
                // Mengubah teks tombol tambah/perbarui menjadi "Perbarui"
                addUpdateButton.setText("Update");
                // Menampilkan tombol hapus
                deleteButton.setVisible(true);
            }
        });
    }

    // Mengatur model tabel dengan data dari database
    public final DefaultTableModel setTable() {
        Object[] column = {"No", "NIM", "Nama", "Jenis Kelamin", "Kelas"};
        DefaultTableModel temp = new DefaultTableModel(null, column);

        try {
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa"); // Mengambil data dari database

            int i = 0;
            while (resultSet.next()) { // Iterasi melalui hasil query
                Object[] row = new Object[5];

                row[0] = i + 1; // Nomor urut
                row[1] = resultSet.getString("nim"); // NIM mahasiswa
                row[2] = resultSet.getString("nama"); // Nama mahasiswa
                row[3] = resultSet.getString("jenis_kelamin"); // Jenis kelamin mahasiswa
                row[4] = resultSet.getString("kelas"); // Kelas mahasiswa

                temp.addRow(row); // Menambahkan baris ke tabel
                i++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e); // Melempar SQLException sebagai RuntimeException
        }

        return temp; // Mengembalikan model tabel yang telah diatur
    }

    // Menambahkan data mahasiswa ke database
    public void insertData() {
        // Mendapatkan data dari inputan pengguna
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String kelas = kelasComboBox.getSelectedItem().toString();

        // Pengecekan apakah NIM sudah ada dalam database
        if (isNIMExist(nim)) {
            JOptionPane.showMessageDialog(null, "NIM sudah ada dalam database", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Keluar dari metode jika NIM sudah ada
        }

        // Membuat query SQL untuk menambahkan data ke database
        String sql = "INSERT INTO mahasiswa values (null, '" + nim + "', '" + nama + "', '" + jenisKelamin + "', '" + kelas + "')";
        database.insertUpdateDeleteQuery(sql); // Menjalankan query

        mahasiswaTable.setModel(setTable()); // Mengatur ulang model tabel
        clearForm(); // Membersihkan form
        JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan"); // Menampilkan pesan berhasil
    }

    // Mengubah data mahasiswa di database
    public void updateData() {
        // Mendapatkan data dari inputan pengguna
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String kelas = kelasComboBox.getSelectedItem().toString();

        // Membuat query SQL untuk mengubah data di database
        String sql = "UPDATE mahasiswa SET nama = '" + nama + "', jenis_kelamin = '" + jenisKelamin + "', kelas = '" + kelas + "' WHERE nim = '" + nim + "'";
        database.insertUpdateDeleteQuery(sql); // Menjalankan query

        mahasiswaTable.setModel(setTable()); // Mengatur ulang model tabel
        clearForm(); // Membersihkan form
        JOptionPane.showMessageDialog(null, "Data berhasil diubah!"); // Menampilkan pesan berhasil
    }

    // Menghapus data mahasiswa dari database
    public void deleteData() {
        String nim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString(); // Mendapatkan NIM dari baris yang dipilih

        // Membuat query SQL untuk menghapus data dari database
        String sql = "DELETE FROM mahasiswa WHERE nim = '" + nim + "'";
        database.insertUpdateDeleteQuery(sql); // Menjalankan query

        mahasiswaTable.setModel(setTable()); // Mengatur ulang model tabel
        clearForm(); // Membersihkan form
        JOptionPane.showMessageDialog(null, "Data berhasil dihapus!"); // Menampilkan pesan berhasil
    }

    // Membersihkan form input
    public void clearForm() {
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        kelasComboBox.setSelectedItem("");
        addUpdateButton.setText("Add");
        deleteButton.setVisible(false);
        nimField.setEnabled(true);
        selectedIndex = -1;
    }

    // Memeriksa apakah input yang dimasukkan oleh pengguna valid
    private boolean isInputValid() {
        // Memeriksa apakah ada kolom yang kosong
        if (nimField.getText().isEmpty() || namaField.getText().isEmpty() || jenisKelaminComboBox.getSelectedItem().toString().isEmpty() || kelasComboBox.getSelectedItem().toString().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Tidak boleh ada kolom kosong", "Error", JOptionPane.ERROR_MESSAGE); // Menampilkan pesan error
            return false; // Mengembalikan false jika input tidak valid
        }
        return true; // Mengembalikan true jika input valid
    }

    // Metode untuk memeriksa apakah NIM sudah ada dalam database
    private boolean isNIMExist(String nim) {
        try {
            ResultSet resultSet = database.selectQuery("SELECT COUNT(*) FROM mahasiswa WHERE nim = '" + nim + "'"); // Mengambil data dari database

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0; // Return true jika nim sudah ada dalam database
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Menampilkan jejak exception jika terjadi SQLException
        }
        return false; // Return false jika nim belum ada dalam database
    }

}