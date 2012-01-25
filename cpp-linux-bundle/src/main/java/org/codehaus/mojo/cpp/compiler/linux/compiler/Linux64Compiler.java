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

package org.codehaus.mojo.cpp.compiler.linux.compiler;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.cpp.compiler.compilation.RecompilationJudge;
import org.codehaus.mojo.cpp.compiler.compilation.gcc.AbstractGccCompiler;
import org.codehaus.mojo.cpp.compiler.settings.CompilationSettings;
import org.codehaus.mojo.cpp.tools.environment.Environment;


public class Linux64Compiler extends AbstractGccCompiler {
	
	public Linux64Compiler(final Log log, final CompilationSettings settings, final Environment targetEnvironment, final RecompilationJudge recompilationJudge) {
		super(log, settings, targetEnvironment, recompilationJudge);
	}

	@Override
	protected String getMandatoryCompilerArguments() {
		return super.getMandatoryCompilerArguments() + " -m64";
	}
}
