
package mg.itu.rohymvc.utilitaire;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class UploadConfig {

    private final Path directory;
    private final long maxFileSize;
    private final boolean overwrite;
    

    private UploadConfig(
            Path directory,
            long maxFileSize,
            boolean overwrite) {
        this.directory = directory;
        this.maxFileSize = maxFileSize;
        this.overwrite = overwrite;
    }

    public static UploadConfig load() throws IOException {
        Properties properties = new Properties();

        ClassLoader loader =
                Thread.currentThread().getContextClassLoader();

        try (InputStream input =
                     loader.getResourceAsStream("rohyupload.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                    "Fichier rohyupload.properties introuvable "
                    + "dans les ressources du projet client."
                );
            }

            properties.load(input);
        }

        String directoryValue =
                properties.getProperty("rohy.upload.directory");

        if (directoryValue == null || directoryValue.isBlank()) {
            throw new IllegalArgumentException(
                "rohy.upload.directory doit être renseigné."
            );
        }

        Path directory = Paths.get(directoryValue).normalize();

        if (!directory.isAbsolute()) {
            throw new IllegalArgumentException(
                "Le répertoire d'upload doit être un chemin absolu."
            );
        }

        long maxFileSize = Long.parseLong(
            properties.getProperty(
                "rohy.upload.max-file-size", "10485760"
            )
        );

        if (maxFileSize <= 0) {
            throw new IllegalArgumentException(
                "La taille maximale doit être positive."
            );
        }

        boolean overwrite = Boolean.parseBoolean(
            properties.getProperty(
                "rohy.upload.overwrite", "false"
            )
        );

        Files.createDirectories(directory);

        if (!Files.isDirectory(directory)
                || !Files.isWritable(directory)) {
            throw new IOException(
                "Répertoire d'upload inaccessible : " + directory
            );
        }

        return new UploadConfig(
            directory, maxFileSize, overwrite
        );
    }

    public Path getDirectory() {
        return directory;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public boolean isOverwrite() {
        return overwrite;
    }
}
