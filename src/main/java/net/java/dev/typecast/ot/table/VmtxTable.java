/*
 * Typecast - The Font Development Environment
 *
 * Copyright (c) 2004-2007 David Schweinsberg
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

package net.java.dev.typecast.ot.table;

import java.io.DataInput;
import java.io.IOException;

/**
 * Vertical Metrics Table
 * @author <a href="mailto:david.schweinsberg@gmail.com">David Schweinsberg</a>
 */
public class VmtxTable extends AbstractTable {

    private int[] _vMetrics;
    private short[] _topSideBearing;

    /**
     * Creates a {@link VmtxTable}.
     */
    public VmtxTable(TableDirectory directory) {
        super(directory);
    }
    
    @Override
    public void read(DataInput di, int length) throws IOException {
        int numberOfLongVerMetrics = vhea().getNumberOfLongVerMetrics();
        _vMetrics = new int[numberOfLongVerMetrics];
        for (int i = 0; i < numberOfLongVerMetrics; ++i) {
            _vMetrics[i] =
                    di.readUnsignedByte()<<24
                    | di.readUnsignedByte()<<16
                    | di.readUnsignedByte()<<8
                    | di.readUnsignedByte();
        }
        int tsbCount = maxp().getNumGlyphs() - numberOfLongVerMetrics;
        _topSideBearing = new short[tsbCount];
        for (int i = 0; i < tsbCount; ++i) {
            _topSideBearing[i] = di.readShort();
        }
    }

    @Override
    public int getType() {
        return vmtx;
    }

    private int getAdvanceHeight(int i) {
        if (_vMetrics == null) {
            return 0;
        }
        if (i < _vMetrics.length) {
            return _vMetrics[i] >> 16;
        } else {
            return _vMetrics[_vMetrics.length - 1] >> 16;
        }
    }

    public short getTopSideBearing(int i) {
        if (_vMetrics == null) {
            return 0;
        }
        if (i < _vMetrics.length) {
            return (short)(_vMetrics[i] & 0xffff);
        } else {
            return _topSideBearing[i - _vMetrics.length];
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("'vmtx' Table - Vertical Metrics\n-------------------------------\n");
//        sb.append("Size = ").append(_de.getLength()).append(" bytes, ")
            sb.append(_vMetrics.length).append(" entries\n");
        for (int i = 0; i < _vMetrics.length; i++) {
            sb.append("        ").append(i)
                .append(". advHeight: ").append(getAdvanceHeight(i))
                .append(", TSdBear: ").append(getTopSideBearing(i))
                .append("\n");
        }
        for (int i = 0; i < _topSideBearing.length; i++) {
            sb.append("        TSdBear ").append(i + _vMetrics.length)
                .append(": ").append(_topSideBearing[i])
                .append("\n");
        }
        return sb.toString();
    }

}
