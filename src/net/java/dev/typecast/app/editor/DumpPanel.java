/*
 * $Id: DumpPanel.java,v 1.1 2007-01-24 09:36:58 davidsch Exp $
 *
 * Typecast - The Font Development Environment
 *
 * Copyright (c) 2004 David Schweinsberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.java.dev.typecast.app.editor;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.java.dev.typecast.app.framework.EditorView;

import net.java.dev.typecast.ot.OTFont;

/**
 * A simple view of an object's "toString()" output.
 * @author <a href="mailto:davidsch@dev.java.net">David Schweinsberg</a>
 * @version $Id: DumpPanel.java,v 1.1 2007-01-24 09:36:58 davidsch Exp $
 */
public class DumpPanel extends JPanel implements EditorView {

    private static final long serialVersionUID = 1L;

    private JTextArea _dumpTextArea;
    
    /** Creates a new instance of DumpPanel */
    public DumpPanel() {
        setName("Dump");
        setLayout(new BorderLayout());
        _dumpTextArea = new JTextArea();
        _dumpTextArea.setEditable(false);
        _dumpTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(_dumpTextArea), BorderLayout.CENTER);
    }
    
    public void setModel(OTFont font, Object obj) {
        _dumpTextArea.setText(obj.toString());
    }
}
