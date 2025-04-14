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

import org.chaostocosmos.leap.common.utils.DateUtils;

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
								//System.out.println(p.toFile().getName());
								return true;
							}
						}
					} catch(Exception e) {
						//System.out.println("Error Path :"+p);
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
	 * @param deleteTgt
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFile(Path src, Path tgt, boolean deleteTgt) throws IOException {
		if(!Files.exists(tgt)) {
			Files.createDirectories(tgt.getParent());
		}
		if(deleteTgt) {
			Files.deleteIfExists(tgt);
		}
		Files.copy(src, tgt, StandardCopyOption.REPLACE_EXISTING);
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
		return fileCopyBackup(Paths.get(src), Paths.get(tgt), Paths.get(bak), true);
	}
	
	/**
	 * Copy file to target and backup
	 * @param srcPath
	 * @param tgtPath
	 * @param bakPath
	 * @param deleteDest
	 * @return
	 * @throws IOException
	 */
	public static boolean fileCopyBackup(Path srcPath, Path tgtPath, Path bakPath, boolean deleteSrc) throws IOException {
		if(!Files.exists(tgtPath.getParent())) {
			Files.createDirectories(tgtPath.getParent());
		}
		if(!Files.exists(bakPath.getParent())) {
			Files.createDirectories(bakPath.getParent());
		}
		if(Files.exists(tgtPath)) {
			Files.delete(tgtPath);
		}
		Files.copy(srcPath, tgtPath, StandardCopyOption.REPLACE_EXISTING);
		if(bakPath != null) {
			Files.copy(srcPath, bakPath, StandardCopyOption.REPLACE_EXISTING);		
		}
		if(deleteSrc) {
			Files.delete(srcPath);
		}
		return true;
	}
	
	/**
	 * Copy directory with source path, target path
	 * @param src
	 * @param dest
	 * @param deleteSrc
	 * @return
	 * @throws IOException
	 */
	public static List<Path> directoryCopy(String src, String dest, boolean deleteSrc) throws IOException {
		return directoryCopy(Paths.get(src), Paths.get(dest), deleteSrc);
	}
	
	/**
	 * Copy directory with source file, destination file, copy option
	 * @param srcDir
	 * @param tgtDir
	 * @param op
	 * @return
	 * @throws IOException
	 */
	public static List<Path> directoryCopy(Path srcDir, Path tgtDir, boolean deleteSrc) throws IOException {
		if(srcDir.equals(tgtDir)) {
			throw new IOException("Source directory is the same with Target directory!!!");
		}
		if(tgtDir.getParent().equals(srcDir)) {
			throw new IOException("Target directory is not right under the source directory!!!");
		}
		if(Files.isDirectory(srcDir)) {
			if(Files.exists(tgtDir)) {
				Files.walk(tgtDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			return Files.walk(srcDir)
						.filter(p -> !Files.isDirectory(p))
						.filter(p -> !p.equals(srcDir))
						.filter(p -> !p.startsWith(tgtDir.getParent()))
						.map(p -> {
				try {					
					String relative = p.toString().substring(srcDir.toString().length()+1);
					Path d = tgtDir.resolve(relative);
					if(!Files.exists(d.getParent())) {
						Files.createDirectories(d.getParent());
					}
					Files.copy(p, d, StandardCopyOption.REPLACE_EXISTING);
					if(deleteSrc && Files.exists(p)) {
						Files.delete(p);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return p;
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
	 * Delete directory with options
	 * @param dest
	 * @param deleteOnExit
	 * @throws IOException
	 */
	public static void directoryDelete(Path dest) throws IOException {
		directoryBackupDelete(dest, null, ".*");
	}
	
	/**
	 * Delete directory with options
	 * @param dest
	 * @param prefix
	 * @throws IOException
	 */
	public static void directoryDelete(Path dest, String prefix) throws IOException {
		directoryBackupDelete(dest, null, prefix+".*");
	}

	/**
	 * Delete directory and backup with options
	 * @param tgtDir
	 * @param bakDir
	 * @throws IOException
	 */
	public static void directoryDelete(Path tgtDir, Path bakDir) throws IOException {
		directoryBackupDelete(tgtDir, bakDir, ".*");
	}
	
	/**
	 * Delete directory and backup with options
	 * @param tgtDir
	 * @param bakDir
	 * @param regex
	 * @throws IOException
	 */
	public static void directoryBackupDelete(Path tgtDir, Path bakDir, String regex) throws IOException {
		if(!Files.exists(tgtDir)) {
			Files.createDirectories(tgtDir);
		}
		if(!Files.isDirectory(tgtDir)) {
			throw new IOException("Delete target must be directory!!!");
		}
		if(bakDir != null) {
			directoryCopy(tgtDir, bakDir, true);
		}
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

	/**
	 * Add backup file suffix
	 * @param path
	 * @return
	 */
	public static Path backupSuffix(Path path) {
		String suffix = DateUtils.getFormattedNow("yyyyMMdd-HHmmss");
		String name = path.getFileName().toString();
		if(name.indexOf(".") != -1) {
			name = name.substring(0, name.indexOf(".")) + suffix + name.substring(name.indexOf("."));
		} else {
			name = name+"-"+suffix;
		}
		return path.getParent().resolve(name);
	}
}

