// package SoundEffect; // Asumsi paket jika tidak ada di file.

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Enum SoundEffect mengelola pemutaran efek suara dalam game Tic-Tac-Toe.
 * Setiap konstanta enum merepresentasikan sebuah efek suara tertentu.
 */
public enum SoundEffect {
   // Definisi konstanta enum dengan jalur file audio masing-masing
   EAT_FOOD("audio/eatfood.wav"), // Efek suara saat membuat langkah
   TOY("audio/toy.wav"),          // Efek suara saat membuat langkah
   DIE("audio/die.wav"),       // Efek suara saat game berakhir (menang/kalah/seri)
   WIN("audio/win.wav");          // Efek suara saat game berakhir (menang/kalah/seri)

   /**
    * Inner enum Volume mendefinisikan tingkat volume yang berbeda untuk efek suara.
    * MUTE: Suara dimatikan.
    * LOW: Volume rendah.
    * MEDIUM: Volume sedang.
    * HIGH: Volume tinggi.
    */
   public static enum Volume {
      MUTE, LOW, MEDIUM, HIGH
   }

   // Atribut statis untuk mengatur volume global semua efek suara
   public static Volume volume = Volume.LOW; // Default volume adalah LOW

   // Objek Clip untuk memutar audio
   private Clip clip;

   /**
    * Konstruktor pribadi untuk enum SoundEffect.
    * Memuat file audio yang ditentukan dan mempersiapkannya untuk pemutaran.
    */
   private SoundEffect(String soundFileName) {
      try {
         // Mendapatkan URL dari resource audio melalui class loader
         URL url = this.getClass().getClassLoader().getResource(soundFileName);
         // Mendapatkan AudioInputStream dari URL
         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
         // Mendapatkan Clip audio
         clip = AudioSystem.getClip();
         // Membuka Clip dengan AudioInputStream
         clip.open(audioInputStream);
      } catch (UnsupportedAudioFileException e) {
         // Menangani jika format file audio tidak didukung
         e.printStackTrace();
      } catch (IOException e) {
         // Menangani jika ada error I/O saat membaca file
         e.printStackTrace();
      } catch (LineUnavailableException e) {
         // Menangani jika jalur audio tidak tersedia
         e.printStackTrace();
      }
   }

   /**
    * Memutar efek suara.
    * Jika volume tidak MUTE, ia akan menghentikan pemutaran sebelumnya (jika ada),
    * mengatur posisi ke awal, dan kemudian memulai pemutaran.
    */
   public void play() {
      if (volume != Volume.MUTE) { // Hanya putar jika volume tidak MUTE
         if (clip.isRunning())
            clip.stop();   // Menghentikan pemutaran jika sedang berjalan
         clip.setFramePosition(0); // Mengatur posisi pemutaran ke awal
         clip.start();     // Memulai pemutaran
      }
   }

   /**
    * Metode statis untuk menginisialisasi semua efek suara.
    * Memanggil values() akan memicu konstruktor untuk semua konstanta enum,
    * sehingga memuat semua file audio.
    */
   static void initGame() {
      values(); // Memastikan semua konstanta enum diinisialisasi (memuat semua suara)
   }
}