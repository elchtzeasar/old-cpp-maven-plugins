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

package org.codehaus.mojo.cpp.tools.environment;

import java.util.Deque;

public class CompoundEnvironment extends Environment {

	private final Environment parent;

	public CompoundEnvironment(String name, Environment parent) {
		super(name);
		this.parent = parent;
	}

	@Override
	public Deque<String> getCandidateNames() {
		Deque<String> q = parent.getCandidateNames();
		q.addFirst(getCanonicalName());
		return q;
	}
}
