/*
MIT License

Copyright (c) 2017 Brian Olson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package org.bolson.glob;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Glob {
    public static Collection<Path> glob(String pattern) {
        FileSystem fs = FileSystems.getDefault();
        Path pp = fs.getPath(pattern);
        boolean abs = pp.isAbsolute();
        Iterator<Path> parts = pp.iterator();
        ArrayList<Path> activePaths = new ArrayList<>();
        {
            Path root = null;
            if (pp.isAbsolute()) {
                root = pp.getRoot();
            } else {
                root = Paths.get(".");
            }
            Path firstpart = parts.next();
            try {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(root, firstpart.toString())) {
                    for (Path match : ds) {
                        activePaths.add(root.resolve(match).normalize());
                    }
                }
            } catch (IOException e) {
                // can't read that, okay
            }
        }
        while (parts.hasNext()) {
            Path part = parts.next();
            ArrayList<Path> prevPaths = activePaths;
            activePaths = new ArrayList<>();
            for (Path root : prevPaths) {
                if (!Files.isDirectory(root)) {
                    continue;
                }
                try {
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(root, part.toString())) {
                        for (Path match : ds) {
                            activePaths.add(root.resolve(match));
                        }
                    }
                } catch (IOException e) {
                    // can't read that, okay
                }
            }
        }
        return activePaths;
    }

    public static void main(String argv[]) {
        for (String arg : argv) {
            for (Path p : glob(arg)) {
                System.out.println(p);
            }
        }
    }
}
