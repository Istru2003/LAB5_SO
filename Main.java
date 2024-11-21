import java.io.*;

public class Main {
    private static final String TEXT_FILE = "document.txt";
    private static final File FIRST_RUN_FILE = new File("first_run.txt");
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) {
        if (!FIRST_RUN_FILE.exists()) {
            addToAutostart();
            try {
                FIRST_RUN_FILE.createNewFile();
            } catch (IOException e) {
                System.out.println("Failed to create first run file.");
            }
        }

        String content = isWindows()
                ? "Hello, this is Laboratory 5 for 'Operating Systems'"
                : "Salut, asta Laboratorul 5 la obiect 'Sisteme de operare'";

        writeToFile(TEXT_FILE, content);
        int exitCode = launchEditor(TEXT_FILE);

        System.out.println("Editor terminated with exit code: " + exitCode);
    }

    private static void writeToFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
            System.out.println("Text has been written to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int launchEditor(String filePath) {
        ProcessBuilder processBuilder = isWindows()
                ? new ProcessBuilder("notepad.exe", filePath)
                : new ProcessBuilder("gedit", filePath);

        try {
            Process process = processBuilder.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void addToAutostart() {
        if (isWindows()) {
            addToAutostartWindows();
        } else {
            addToAutostartLinux();
        }
    }

    private static void addToAutostartWindows() {
        String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
        String fileAbsolutePath = new File(TEXT_FILE).getAbsolutePath();

        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe",
                "/c",
                "reg add \"" + key + "\" /v NewDoc /t REG_SZ /d \"notepad.exe \\\"" + fileAbsolutePath + "\\\"\" /f"
        );

        try {
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Successfully added to autostart");
            } else {
                System.out.println("Error adding to autostart. Code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error executing registry command");
        }
    }

    private static void addToAutostartLinux() {
        String autostartDir = System.getProperty("user.home") + "/.config/autostart";
        File autostartFile = new File(autostartDir + "/texteditor.desktop");

        if (autostartFile.exists()) {
            System.out.println("Autostart entry already exists.");
            return;
        }

        new File(autostartDir).mkdirs();

        String desktopEntry = String.format(
                "[Desktop Entry]\n" +
                        "Type=Application\n" +
                        "Name=TextEditor\n" +
                        "Exec=gedit %s\n" +
                        "Terminal=false\n" +
                        "X-GNOME-Autostart-enabled=true",
                new File(TEXT_FILE).getAbsolutePath()
        );

        try (FileWriter writer = new FileWriter(autostartFile)) {
            writer.write(desktopEntry);
            Runtime.getRuntime().exec("chmod +x " + autostartFile.getAbsolutePath());
            System.out.println("Successfully added to autostart");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error adding to autostart");
        }
    }

    private static boolean isWindows() {
        return OS.contains("win");
    }
}