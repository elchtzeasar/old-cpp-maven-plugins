/* 
 *  Copyright 2011 Ericsson AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.codehaus.mojo.cpp.compiler.artifacts.executables;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.codehaus.mojo.cpp.compiler.files.NativeCodeFile;


public class Executable {
	private final String name;
	private final Collection<NativeCodeFile> nativeCodeFiles;
	private final Collection<File> rawFilesToLink;

	public Executable(final String name, final NativeCodeFile nativeCodeFile) {
		this(name, Arrays.asList(new NativeCodeFile[] { nativeCodeFile }));
	}

	public Executable(final String name, final Collection<NativeCodeFile> nativeCodeFiles) {
		this(name, nativeCodeFiles, new ArrayList<File>());
	}

	public Executable(final String name, final Collection<NativeCodeFile> nativeCodeFiles, final Collection<File> rawFilesToLink) {
		this.name = name;
		this.nativeCodeFiles = nativeCodeFiles;
		this.rawFilesToLink = rawFilesToLink;
	}

	public String getName() {
		return name;
	}

	public Collection<NativeCodeFile> getNativeCodeFiles() {
		return nativeCodeFiles;
	}

	public void addRawFilesToLink(final Collection<File> rawFiles) {
		rawFilesToLink.addAll(rawFiles);
	}

	public Collection<File> getAllFilesToLink() {
		final Collection<File> objectFiles = new ArrayList<File>();

		for(NativeCodeFile nativeCodeFile : nativeCodeFiles)
			objectFiles.add(nativeCodeFile.getObjectFile());

		objectFiles.addAll(rawFilesToLink);

		return objectFiles;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
