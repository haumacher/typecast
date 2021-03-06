/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included with this distribution in  * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/

package net.java.dev.typecast.ot.table;

import java.io.DataInput;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.java.dev.typecast.io.BinaryOutput;
import net.java.dev.typecast.io.Writable;
import net.java.dev.typecast.ot.Fmt;

/**
 * loca — Index to Location Table
 * 
 * <p>
 * The indexToLoc table stores the offsets to the locations of the glyphs in the
 * font, relative to the beginning of the glyphData table. In order to compute
 * the length of the last glyph element, there is an extra entry after the last
 * valid index.
 * </p>
 * 
 * <p>
 * By definition, index zero points to the “missing character”, which is the
 * character that appears if a character is not found in the font. The missing
 * character is commonly represented by a blank box or a space. If the font does
 * not contain an outline for the missing character, then the first and second
 * offsets should have the same value. This also applies to any other characters
 * without an outline, such as the space character. If a glyph has no outline,
 * then loca[n] = loca[n+1]. In the particular case of the last glyph(s),
 * loca[n] will be equal the length of the glyph data ('glyf') table. The
 * offsets must be in ascending order with loca[n] <= loca[n+1].
 * </p>
 * 
 * <p>
 * Most routines will look at the 'maxp' table to determine the number of glyphs
 * in the font, but the value in the 'loca' table must agree.
 * </p>
 * 
 * <p>
 * There are two versions of this table: a short version, and a long version.
 * The version is specified in the indexToLocFormat entry in the 'head' table.
 * </p>
 * 
 * @author <a href="mailto:david.schweinsberg@gmail.com">David Schweinsberg</a>
 * 
 * @see <a href="https://docs.microsoft.com/en-us/typography/opentype/spec/loca">Spec: Index to Location</a>
 */
public class LocaTable extends AbstractTable implements Writable {

    private int[] _offsets;

    private static final Logger logger = LoggerFactory.getLogger(LocaTable.class);

    /**
     * Creates a {@link LocaTable}.
     */
    public LocaTable(TableDirectory directory) {
        super(directory);
    }
    
    @Override
    public void read(DataInput di, int length) throws IOException {
        int numGlyphs = maxp().getNumGlyphs();
        _offsets = new int[numGlyphs + 1];
        boolean shortEntries = head().useShortEntries();
        if (shortEntries) {
            for (int i = 0; i <= numGlyphs; i++) {
                _offsets[i] = 2 * di.readUnsignedShort();
            }
        } else {
            for (int i = 0; i <= numGlyphs; i++) {
                _offsets[i] = di.readInt();
            }
        }
        
        // Check the validity of the offsets
        int lastOffset = 0;
        int index = 0;
        for (int offset : _offsets) {
            if (offset < lastOffset) {
                logger.error("Offset at index {} is bad ({} < {})", index, offset, lastOffset);
            }
            lastOffset = offset;
            ++index;
        }
    }
    
    void updateFormat() {
        HeadTable headTable = head();
        for (int offset : _offsets) {
            if (offset > 2 * 0xFFFF || offset % 2 != 0) {
                headTable.setShortEntries(false);
                return;
            }
        }
        headTable.setShortEntries(true);
    }
    
    @Override
    public void write(BinaryOutput out) throws IOException {
        boolean shortEntries = head().useShortEntries();
        if (shortEntries) {
            for (int offset : _offsets) {
                out.writeShort(offset / 2);
            }
        } else {
            for (int offset : _offsets) {
                out.writeInt(offset);
            }
        }
    }
    
    @Override
    public int getType() {
        return loca;
    }

    public int getOffset(int glyphId) {
        return _offsets[glyphId];
    }
    
    public void setOffset(int glyphId, int offset) {
        _offsets[glyphId] = offset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("'loca' Table - Index To Location Table\n");
        sb.append("--------------------------------------\n");
        sb.append("    entries = " + _offsets.length + "\n");
        for (int i = 0; i < _offsets.length; i++) {
            sb.append("    Index " + Fmt.pad(5, i) + " -> Offset " + Fmt.pad(7, getOffset(i)) + "\n");
        }
        return sb.toString();
    }
    
}
