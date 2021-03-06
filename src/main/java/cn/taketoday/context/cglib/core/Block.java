/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.taketoday.context.cglib.core;

import cn.taketoday.context.asm.Label;

public class Block {
  private Label end;
  private final Label start;
  private final CodeEmitter e;

  public Block(CodeEmitter e) {
    this.e = e;
    start = e.mark();
  }

  public CodeEmitter getCodeEmitter() {
    return e;
  }

  public void end() {
    if (end != null) {
      throw new IllegalStateException("end of label already set");
    }
    end = e.mark();
  }

  public Label getStart() {
    return start;
  }

  public Label getEnd() {
    return end;
  }
}
