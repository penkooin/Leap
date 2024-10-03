package org.chaostocosmos.leap.common.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * FileTools
 * 
 * @author 9ins
 * @date 2019. 11. 1.
 *
 */
public class FileTools {

	/**
	 * Get all java files including specific word list.
	 * @param dir
	 * @param wordList
	 * @return
	 * @throws IOException
	 */
	public static List<Path> getAllJavaFilesIncludeWord(File dir, List<String> wordList) throws IOException {
		return getAllFilesIncludeWord(dir, wordList, ".java");
	}
	
	/**
	 * Get all files including specific words.
	 * @param dir
	 * @param wordList
	 * @return
	 * @throws IOException 
	 */
	public static List<Path> getAllFilesIncludeWord(File dir, List<String> wordList, String suffix) throws IOException {
		return Files.walk(Paths.get(dir.getAbsolutePath())).filter(p -> {
			if(p.toFile().isFile()) {
				if( !isBinaryFile(p.toFile()) || p.toFile().getName().endsWith(suffix)) {				
					try {
						List<String> allLines = Files.readAllLines(p, Charset.forName("utf-8"));
						for(String w : wordList) {
							if(allLines.stream().anyMatch(l -> l.contains(w))) {
								System.out.println(p.toFile().getName());
								return true;
							}
						}
					} catch(Exception e) {
						System.out.println("Error Path :"+p);
						e.printStackTrace();
					}
					return false;
				}
			}
			return false;
		}).collect(Collectors.toList());
	}
	
	/**
	 * Get all files with prefix/suffix
	 * @param path
	 * @param isFile
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static List<File> getAllFilesWithPrefixSuffix(File path, boolean isFile, String prefix, String suffix) {
		return getAlls(path, isFile, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(prefix) && name.endsWith(suffix);
			}
		});
	}
	
	/**
	 * Get all files with prefix
	 * @param path
	 * @param isFile
	 * @param prefix
	 * @return
	 */
	public static List<File> getAllFilesWithPrefix(File path, boolean isFile, String prefix) {
		return getAlls(path, isFile, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				//System.out.println(name);
				return name.startsWith(prefix);
			}			
		});		
	}
	
	/**
	 * Get all files with suffix
	 * @param path
	 * @param isFile
	 * @param suffix
	 * @return
	 */
	public static List<File> getAllFilesWithSuffix(File path, boolean isFile, String suffix) {
		return getAlls(path, isFile, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(suffix);
			}			
		});
	}
	
	/**
	 * Get all directory
	 * @param path
	 * @param filter
	 * @return
	 */
	public static List<File> getAllDir(File path, FilenameFilter filter) {
		return getAlls(path, false, filter);
	}
	
	/**
	 * Get all files
	 * @param path
	 * @param filter
	 * @return
	 */
	public static List<File> getAllFiles(File path, FilenameFilter filter) {
		return getAlls(path, true, filter);
	}
	
	/**
	 * Get all
	 * @param path
	 * @param isFile
	 * @param filter
	 * @return
	 */
	public static List<File> getAlls(File path, boolean isFile, FilenameFilter filter) {
		if(isFile) {
			return getAlls(new ArrayList<File>(), path, filter).stream().filter(f -> f.isFile()).collect(Collectors.toList());
		} else {
			return getAlls(new ArrayList<File>(), path, filter).stream().filter(f -> f.isDirectory()).collect(Collectors.toList());
		}
	}
	
	/**
	 * Get all
	 * @param path
	 * @return
	 */
	public static List<File> getAlls(File path) {
		return getAlls(new ArrayList<File>(), path, null);
	}
	
	/**
	 * Get all
	 * @param fileList
	 * @param path
	 * @param filter
	 * @return
	 */
	public static List<File> getAlls(List<File> fileList, File path, FilenameFilter filter) {
		File[] files = filter != null ? path.listFiles(filter) : path.listFiles();
		for(File file : files) {
			if(file.isDirectory()) {
				fileList.add(file);
				getAlls(fileList, file, filter);			
			} else {
				fileList.add(file);
			}
		}
		return fileList;
	}
	
	/**
	 * Copy file to target with target deleting option
	 * @param src
	 * @param tgt
	 * @param deleteTarget
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFile(File src, File tgt, boolean deleteTarget) throws IOException {
		Path srcPath = Paths.get(src.getAbsolutePath());
		Path tgtPath = Paths.get(tgt.getAbsolutePath());
		if(!tgt.getParentFile().exists()) {
			tgt.getParentFile().mkdirs();
		}
		if(deleteTarget) {
			Files.deleteIfExists(tgtPath);
		}
		Files.copy(srcPath, tgtPath, StandardCopyOption.REPLACE_EXISTING);
		return true;
	}
	
	/**
	 * Copy file to target and backup
	 * @param src
	 * @param tgt
	 * @param bak
	 * @return
	 * @throws IOException
	 */
	public static boolean fileCopyBackup(String src, String tgt, String bak) throws IOException {
		return fileCopyBackup(new File(src), new File(tgt), new File(bak), true);
	}
	
	/**
	 * Copy file to target and backup
	 * @param srcFile
	 * @param tgtFile
	 * @param bakFile
	 * @param deleteDest
	 * @return
	 * @throws IOException
	 */
	public static boolean fileCopyBackup(File srcFile, File tgtFile, File bakFile, boolean deleteSrc) throws IOException {
		Path srcPath = Paths.get(srcFile.getAbsolutePath());
		Path tgtPath = Paths.get(tgtFile.getAbsolutePath());
		Path bakPath = Paths.get(bakFile.getAbsolutePath());
		if(!tgtFile.exists()) {
			tgtFile.getParentFile().mkdirs();
		}
		if(!bakFile.exists()) {
			bakFile.getParentFile().mkdirs();
		}		
		if(tgtFile.exists()) {
			Files.delete(Paths.get(tgtFile.getCanonicalPath()));
		}
		Files.copy(Paths.get(srcFile.getCanonicalPath()), Paths.get(tgtFile.getCanonicalPath()), StandardCopyOption.REPLACE_EXISTING);
		if(bakPath != null) {
			Files.copy(srcPath, bakPath, StandardCopyOption.REPLACE_EXISTING);		
		}
		if(deleteSrc) {
			Files.delete(Paths.get(srcFile.getAbsolutePath()));
		}
		return true;
	}
	
	/**
	 * Copy directory with source path, target path
	 * @param src
	 * @param dest
	 * @return
	 * @throws IOException
	 */
	public static List<File> directoryCopy(String src, String dest, boolean deleteSrc) throws IOException {
		return directoryCopy(new File(src), new File(dest), deleteSrc);
	}
	
	/**
	 * Copy directory with source file, destination file, copy option
	 * @param srcDir
	 * @param tgtDir
	 * @param op
	 * @return
	 * @throws IOException
	 */
	public static List<File> directoryCopy(File srcDir, File tgtDir, boolean deleteSrc) throws IOException {
		if(srcDir.isDirectory()) {
			Files.walk(Paths.get(tgtDir.getAbsolutePath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			if(!tgtDir.exists()) {
				tgtDir.mkdirs();
			}
			Path srcPath = Paths.get(srcDir.getAbsolutePath());
			Path destPath = Paths.get(tgtDir.getAbsolutePath());
			return Files.walk(srcPath).filter(f -> f.toFile().compareTo(srcDir) != 0 && !f.toFile().getAbsolutePath().startsWith(srcDir.getAbsolutePath()+File.separator+"patch")).map(p -> {
				try {
					Path d = destPath.resolve(srcPath.relativize(p));
					Files.copy(p, d, StandardCopyOption.REPLACE_EXISTING);
					if(deleteSrc && !p.toFile().getAbsolutePath().equals(srcDir.getCanonicalPath())) {
						Files.delete(p);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return p.toFile();
			}).collect(Collectors.toList());
		} else {
			throw new IOException("Source must be to directory!!!");
		}
	}
	
	/**
	 * Copy directory to target and backup
	 * @param srcDir
	 * @param tgtFile
	 * @param bakDir
	 * @param deleteDest
	 * @return
	 * @throws IOException
	 */
	public static boolean directoryCopyBackup(File srcDir, File tgtDir, File bakDir, boolean deleteSrc) throws IOException {
		if(!srcDir.isDirectory()) {
			throw new IOException("Path must be directory!!!");
		}
		Path srcPath = Paths.get(srcDir.getAbsolutePath());
		Path tgtPath = Paths.get(tgtDir.getAbsolutePath());
		
		Files.walk(tgtPath).sorted(Comparator.reverseOrder()).map(p -> p.toFile().getAbsolutePath().startsWith(srcDir.getParentFile().getAbsolutePath()) ? null : p.toFile()).filter(f -> f != null).forEach(File::delete);
		if(bakDir != null) {
			Files.walk(Paths.get(bakDir.getAbsolutePath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}
		if(!tgtDir.exists()) {
			tgtDir.mkdirs();
		}
		if(bakDir != null &&!bakDir.exists()) {
			bakDir.mkdirs();
		}
		Files.walk(srcPath).collect(Collectors.toList()).stream().forEach(p -> {
			try {
				Path dest = tgtPath.resolve(srcPath.relativize(p));
				Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
				if(bakDir != null) {
					Path bakDest = Paths.get(bakDir.getAbsolutePath()).resolve(srcPath.relativize(p));
					Files.copy(p, bakDest, StandardCopyOption.REPLACE_EXISTING);
				}
				if(deleteSrc && !p.toFile().getAbsolutePath().equals(srcDir.getAbsolutePath())) {
					Files.delete(p);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return true;
	}
	
	/**
	 * Copy source to destination directories and backup 
	 * @param srcDir
	 * @param destDirs
	 * @param bakDir
	 * @param deleteDest
	 * @return
	 * @throws IOException
	 */
	public static void directoryCopysBackup(File srcDir, List<File> destDirs, File bakDir, boolean deleteSrc) throws IOException {
		if(srcDir.isFile()) {
			throw new IOException("Source file must be to directory!!!");
		}
		try {
			Path srcPath = Paths.get(srcDir.getAbsolutePath());
			Path bakPath = Paths.get(bakDir.getAbsolutePath());
			List<Path> destPaths = destDirs.stream().map(f -> Paths.get(f.getAbsolutePath())).collect(Collectors.toList());
			Files.walk(srcPath).filter(f -> f.toFile().compareTo(srcDir) != 0).forEach(p -> {
				//System.out.println(p.toString());
				destPaths.stream().forEach(f -> {
					try {
						if(f.toFile().isFile()) {
							throw new IOException("Destination file must be to directory!!!");
						}
						if(!f.toFile().exists()) {
							f.toFile().mkdirs();
						}
						Path destPath = f.resolve(srcPath.relativize(p));
						if(destPath.toFile().isDirectory()) {
							destPath.toFile().mkdirs();
						} else {
							Files.copy(p, destPath, StandardCopyOption.REPLACE_EXISTING);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				});
			});
			if(bakDir != null) {
				if(!bakDir.exists()) {
					bakDir.mkdirs();
				}
				Files.walk(srcPath).filter(f -> f.toFile().compareTo(srcDir) != 0 && !f.toFile().getAbsolutePath().startsWith(bakDir.getAbsolutePath())).forEach(p -> {
					try {
						Path destPath = bakPath.resolve(srcPath.relativize(p));
						if(destPath.toFile().isDirectory()) {
							destPath.toFile().mkdirs();
						} else {			
							Files.copy(p, destPath, StandardCopyOption.REPLACE_EXISTING);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				});
			}
			if(deleteSrc) {
				Files.walk(Paths.get(srcDir.getAbsolutePath())).map(Path::toFile).forEach(File::delete);
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Move directory to destination and backup
	 * @param src
	 * @param tgt
	 * @param bak
	 * @return
	 * @throws IOException
	 */
	public static boolean directoryMoveBackup(String src, String tgt, String bak) throws IOException {
		return directoryMoveBackup(new File(src), new File(tgt), new File(bak));
	}
	
	/**
	 * Move directory to destination and backup
	 * @param src
	 * @param tgt
	 * @param bak
	 * @return
	 * @throws IOException
	 */
	public static boolean directoryMoveBackup(File src, File tgt, File bak) throws IOException {
		directoryCopyBackup(src, tgt, bak, true);
		return true;
	}

	/**
	 * Delete directory
	 * @param dest
	 * @return
	 * @throws IOException
	 */
	public static List<String> directoryDelete(File dest) throws IOException {
		return directoryDelete(dest, true, false);
	}
	
	/**
	 * Delete directory with options
	 * @param dest
	 * @param deleteRoot
	 * @param deleteOnExit
	 * @return
	 * @throws IOException
	 */
	public static List<String> directoryDelete(File dest, boolean deleteRoot, boolean deleteOnExit) throws IOException {
		return directoryBackupDelete(dest, null, "*", deleteRoot, deleteOnExit);
	}
	
	/**
	 * Delete directory with options
	 * @param dest
	 * @param prefix
	 * @return
	 * @throws IOException
	 */
	public static List<String> directoryDelete(File dest, String prefix) throws IOException {
		return directoryBackupDelete(dest, null, prefix+".*", false, false);
	}

	/**
	 * Delete directory and backup with options
	 * @param tgtDir
	 * @param bakDir
	 * @return
	 * @throws IOException
	 */
	public static List<String> directoryDelete(File tgtDir, File bakDir) throws IOException {
		return directoryBackupDelete(tgtDir, bakDir, "*", true, false);
	}
	
	/**
	 * Delete directory and backup with options
	 * @param tgtDir
	 * @param bakDir
	 * @param regex
	 * @param deleteRoot
	 * @param deleteOnExit
	 * @return
	 * @throws IOException
	 */
	public static List<String> directoryBackupDelete(File tgtDir, File bakDir, String regex, boolean deleteRoot, boolean deleteOnExit) throws IOException {
		if(!tgtDir.isDirectory()) {
			throw new IOException("Delete target must be directory!!!");
		}
		if(bakDir != null) {
			directoryCopy(tgtDir, bakDir, true);
		}
		List<String> deletedFileList = new ArrayList<String>();
		Path destPath = Paths.get(tgtDir.getAbsolutePath());
		Files.walk(destPath)
			.filter(p -> p.toFile().getName().matches(regex))
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(d -> {
				if(deleteOnExit) {
					try {
						Files.walk(Paths.get(d.getAbsolutePath())).sorted(Comparator.reverseOrder()).map(p -> {
							File f = p.toFile();
							//System.out.println("@@@@ "+f.getAbsolutePath());
							deletedFileList.add(f.getAbsolutePath());
							return f;
						}).forEach(File::deleteOnExit);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						Files.walk(Paths.get(d.getAbsolutePath())).sorted(Comparator.reverseOrder()).map(p -> {
							File f = p.toFile();
							//System.out.println("@@@@ "+f.getAbsolutePath());
							deletedFileList.add(f.getAbsolutePath());
							return f;
						}).forEach(File::delete);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		});
		if(deleteRoot) {
			if(deleteOnExit) {
				tgtDir.deleteOnExit();
			} else {
				tgtDir.delete();
			}
		}
		return deletedFileList;
	}
	
	/**
	 * Delete all sub directories and files
	 * @param dest
	 * @param deleteOnExit
	 * @throws IOException
	 */
	public static void deleteAllSubDirectory(File dest, boolean deleteOnExit) throws IOException {
		if(deleteOnExit) {
			Files.walk(Paths.get(dest.getCanonicalPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::deleteOnExit);
		} else {
			Files.walk(Paths.get(dest.getCanonicalPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}
	}
	
	/**
	 * Whether binary file.
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static boolean isBinaryFile(File f) {
		try {
	        String type = Files.probeContentType(f.toPath());
	        if (type == null) {
	            //type couldn't be determined, assume binary
	            return true;
	        } else if (type.startsWith("text")) {
	            return false;
	        } else {
	            //type isn't text
	            return true;
	        }
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
    }
}

