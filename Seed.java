// package Seed; // Asumsi paket jika tidak ada di file.

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Enum Seed mendefinisikan tiga kemungkinan state atau simbol untuk pemain dan sel:
 * CROSS (untuk 'X'), NOUGHT (untuk 'O'), dan NO_SEED (untuk sel kosong).
 * Setiap Seed juga menyimpan representasi tampilan (displayName) dan gambar (img) yang sesuai.
 */
public enum Seed {
   // Definisi konstanta enum dengan nama tampilan dan jalur gambar
   CROSS("X", "images/cross new.png"),    // Simbol X
   NOUGHT("O", "images/nought new.png"),  // Simbol O
   NO_SEED(" ", null);                    // Sel kosong, tanpa gambar

   // Atribut pribadi untuk setiap konstanta enum
   private String displayName; // Nama tampilan dari Seed (misalnya, "X", "O", " ")
   private Image img = null;   // Objek gambar yang terkait dengan Seed

   /**
    * Konstruktor pribadi untuk enum Seed.
    * Menginisialisasi displayName dan mencoba memuat gambar dari file yang diberikan.
    */
   private Seed(String name, String imageFilename) {
      this.displayName = name; // Mengatur nama tampilan

      // Memuat gambar jika imageFilename tidak null
      if (imageFilename != null) {
         // Mendapatkan URL resource gambar dari class loader
         URL imgURL = getClass().getClassLoader().getResource(imageFilename);
         ImageIcon icon = null;
         if (imgURL != null) {
            icon = new ImageIcon(imgURL); // Membuat ImageIcon dari URL
         } else {
            // Menampilkan error jika file gambar tidak ditemukan
            System.err.println("Couldn't find file " + imageFilename);
         }
         img = icon != null ? icon.getImage() : null; // Mendapatkan objek Image dari ImageIcon
      }
   }

   /**
    * Mengembalikan nama tampilan dari Seed ini.
    */
   public String getDisplayName() {
      return displayName;
   }

   /**
    * Mengembalikan objek Image yang terkait dengan Seed ini.
    */
   public Image getImage() {
      return img;
   }
}