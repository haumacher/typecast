/*
 * $Id: CharacterMap.java,v 1.2 2004-12-21 16:55:48 davidsch Exp $
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

package net.java.dev.typecast.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.geom.AffineTransform;

import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.java.dev.typecast.ot.OTFont;

import net.java.dev.typecast.ot.table.CmapFormat;

import net.java.dev.typecast.render.GlyphImageFactory;

/**
 * An editor for the character-to-glyph map, as represented in the CmapTable.
 * @author <a href="mailto:davidsch@dev.java.net">David Schweinsberg</a>
 * @version $Id: CharacterMap.java,v 1.2 2004-12-21 16:55:48 davidsch Exp $
 */
public class CharacterMap extends JScrollPane {

    private static final long serialVersionUID = 1L;
    
    private static final int CELL_WIDTH = 48;
    private static final int CELL_HEIGHT = 60;
    
    private AbstractListModel _listModel;
    private OTFont _font;
    private AffineTransform _tx;
    private Font _labelFont = new Font("SansSerif", Font.PLAIN, 10);
    
    private class Mapping {

        private int _charCode;
        private int _glyphCode;
        private Image _glyphImage;
        
        public Mapping(int charCode, int glyphCode) {
            _charCode = charCode;
            _glyphCode = glyphCode;
        }

        public int getCharCode() {
            return _charCode;
        }
        
        public int getGlyphCode() {
            return _glyphCode;
        }
        
        public Image getGlyphImage() {
            if (_glyphImage == null) {
                _glyphImage = GlyphImageFactory.buildImage(
                        _font.getGlyph(_glyphCode),
                        _tx,
                        CELL_WIDTH,
                        CELL_HEIGHT - 10);
            }
            return _glyphImage;
        }
    }
    
    private class CharListCellRenderer extends JComponent implements ListCellRenderer {

        private static final long serialVersionUID = 1L;
        
        private Mapping _mapping;
        private boolean _isSelected;
        private AffineTransform _imageTx =
                new AffineTransform(1.0, 0.0, 0.0, 1.0, 0.0, 10.0);
        
        /**
         * Renders each individual cell
         */
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(Color.BLACK);
            
            if (_isSelected) {
                g2d.fillRect(0, 0, CELL_WIDTH, 10);
                g2d.setColor(Color.WHITE);
            }
            
            // Label this cell with the character code
            g2d.setFont(_labelFont);
            g2d.drawString(String.format("%04X", _mapping.getCharCode()), 1, 9);
            
            // Draw the glyph
            g2d.drawImage(_mapping.getGlyphImage(), _imageTx, null);
        }
        
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            _mapping = (Mapping) value;
            _isSelected = isSelected;
            setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
            return this;
        }
    }
    
    /** Creates a new instance of CharacterMap */
    public CharacterMap(OTFont font, final CmapFormat cmapFormat) {

        _font = font;

        // Set up a list model to wrap the cmap
        AbstractListModel _listModel = new AbstractListModel() {
            
            private static final long serialVersionUID = 1L;
            private CmapFormat _cmapFormat  = cmapFormat;
            private ArrayList<Mapping> _mappings = new ArrayList<Mapping>();

            {
                for (int i = 0; i < _cmapFormat.getRangeCount(); ++i) {
                    CmapFormat.Range range = _cmapFormat.getRange(i);
                    for (int j = range.getStartCode(); j <= range.getEndCode(); ++j) {
                        _mappings.add(new Mapping(j, _cmapFormat.mapCharCode(j)));
                    }
                }
            }

            public Object getElementAt(int index) {
                return _mappings.get(index);
            }
            
            public int getSize() {
                return _mappings.size();
            }
        };
        
        final JList list = new JList(_listModel);
        list.setCellRenderer(new CharListCellRenderer());
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(
                _listModel.getSize() / 16 +
                (_listModel.getSize() % 16 > 0 ? 1 : 0));
        setViewportView(list);

        // Create a mouse listener so we can listen to double-clicks
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                }
            }
        };
        list.addMouseListener(mouseListener);

//        int unitsPerEmBy2 = _font.getHeadTable().getUnitsPerEm() / 2;
//        int translateX = 2 * unitsPerEmBy2;
//        int translateY = 2 * unitsPerEmBy2;
        
        // How much should we scale the font to fit it into our tint bitmap?
        double scaleFactor = 40.0 / _font.getHeadTable().getUnitsPerEm();
        
        _tx = new AffineTransform();
        _tx.translate(2, CELL_HEIGHT - 20);
        _tx.scale(scaleFactor, scaleFactor);
    }
}
