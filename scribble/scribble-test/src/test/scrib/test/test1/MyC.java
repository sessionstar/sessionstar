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

package test.test1;

import org.scribble.runtime.message.ObjectStreamFormatter;
import org.scribble.runtime.net.SocketChannelEndpoint;
import org.scribble.runtime.session.MPSTEndpoint;
import org.scribble.runtime.util.Buf;

import test.test1.Test1.Proto1.Proto1;
import test.test1.Test1.Proto1.roles.C;
import test.test1.Test1.Proto1.statechans.C.Proto1_C_1;
import test.test1.Test1.Proto1.statechans.C.Proto1_C_2;

public class MyC
{
	public static void main(String[] args) throws Exception
	{
		Proto1 adder = new Proto1();
		try (MPSTEndpoint<Proto1, C> se = new MPSTEndpoint<>(adder, Proto1.C,
				new ObjectStreamFormatter()))
		{
			se.request(Proto1.S, SocketChannelEndpoint::new, "localhost", 8888);

			Proto1_C_2 s2 = new Proto1_C_1(se).send(Proto1.S, Proto1._1);
			for (int i = 0; i < 3; i++)
			{
				s2 = 
					s2.send(Proto1.S, Proto1._2, 123)
					  .receive(Proto1.S, Proto1._3, new Buf<>())
					  .send(Proto1.S, Proto1._1);
			}
			s2.send(Proto1.S, Proto1._4).end();

			/*for (int i = 0; i < 3; i++)
			{
				s1 =
					s1.send(Proto1.S, Proto1._1)
					  .send(Proto1.S, Proto1._2, 123)
					  .receive(Proto1.S, Proto1._3, new Buff<>());
			}
			s1.send(Proto1.S, Proto1._1).send(Proto1.S, Proto1._4).end();*/
		}
	}
}
