package com.cartaxi.common.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadPathResolver {

    private UploadPathResolver() {
    }

    public static Path resolveBasePath(String configuredBaseDir) {
        Path configuredPath = Paths.get(configuredBaseDir);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }

        Path workingDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path backendDir = isBackendDir(workingDir) ? workingDir : workingDir.resolve("backend").normalize();
        if (Files.isDirectory(backendDir) || isBackendDir(backendDir)) {
            return backendDir.resolve(configuredBaseDir).normalize();
        }
        return workingDir.resolve(configuredBaseDir).normalize();
    }

    public static String toResourceLocation(Path directoryPath) {
        String location = directoryPath.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    private static boolean isBackendDir(Path path) {
        return path.getFileName() != null && "backend".equalsIgnoreCase(path.getFileName().toString());
    }
}
