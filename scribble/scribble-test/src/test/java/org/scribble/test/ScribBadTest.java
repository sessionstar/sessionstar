/**
 * Copyright 2008 The Scribble Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.scribble.test;

import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// For running all tests under the specified directory as bad tests
// Needs -Dtest.dir=[test root dir] system property -- Eclipse VM arg: -Dtest.dir=${selected_resource_loc} 
//@RunWith(value = Parameterized.class)
@RunWith(Parameterized.class)
public class ScribBadTest extends ScribTestBase
{
	protected static final String BAD_DIR = "bad";

	public ScribBadTest(String example, boolean isBadTest)
	{
		super(example, isBadTest);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> data()
	{
		return Harness.checkTestDirProperty(ScribTestBase.BAD_TEST, ScribBadTest.BAD_DIR);
	}
}
