//ModDisable
//A Minecraft Mod to disable other Mods
//Copyright (C) 2024 Kaloyan Ivanov Donev

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.


package com.kaloyandonev.moddisable.helpers;

import com.kaloyandonev.moddisable.commands.Disable_Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyFolderContents {

    private static final Logger logger = LogManager.getLogger();

    public void CopyFolderContents(Path sourceDir, Path destinationDir) throws IOException {
        if (Files.notExists(destinationDir)) {
            try {
                Files.createDirectories(destinationDir);
            } catch (IOException e) {
                logger.error("[Mod Disable] [CopyFolderContents] Trying to create the destination folder resulted in a IOException. Exiting early.");
                return;
            }
        }

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = sourceDir.relativize(file);
                Path targetPath = destinationDir.resolve(relativePath);


                if (Files.notExists(targetPath.getParent())) {
                    Files.createDirectories(targetPath.getParent());
                    logger.info("[Mod Disable] [CopyFolderContents] Destination directory does not exist, creating");
                }



                Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
