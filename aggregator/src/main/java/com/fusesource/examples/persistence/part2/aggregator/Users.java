package com.fusesource.examples.persistence.part2.aggregator;

/**
  * Copyright 2011 FuseSource
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
 */

import org.apache.camel.Exchange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {

    private static List<String> aList = new ArrayList<String>();
    private static int count = -1;

    static {
        aList.add("Charles, ");
        aList.add("Raul, ");
        aList.add("Emma, ");
        aList.add("Corine, ");
        aList.add("James, ");
        aList.add("Rich, ");
        aList.add("Claus, ");
        aList.add("Hiram, ");
        aList.add("Gert, ");
        aList.add("Willem, ");
        aList.add("Larry, ");
        aList.add("Matt.");
    }

    public void getUser(Exchange ex) throws Exception {
        if (count <= 10) {
            count++;
        }

        if (count == 3) {
            // throw new Exception("$$$ The machine has crashed.");
        }

        ex.getIn().setHeader("id","FUSE");
        ex.getIn().setBody(aList.get(count));

    }

}
