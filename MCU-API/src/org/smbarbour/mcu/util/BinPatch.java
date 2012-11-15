/*
 * This class is based heavily upon the JBPatch and Util class found at
 * http://alvinalexander.com/java/jwarehouse/eclipse/org.eclipse.equinox/p2/bundles/ie.wombat.jbdiff/src/ie/wombat/jbdiff/JBPatch.java.shtml
 * and
 * http://alvinalexander.com/java/jwarehouse/eclipse/org.eclipse.equinox/p2/bundles/ie.wombat.jbdiff/src/ie/wombat/jbdiff/Util.java.shtml
 *
 * This class should, however, support the bsdiff format utilizing BZip2 instead of GZip
 * 
 * Copyright (c) 2005, Joe Desbonnet, (jdesbonnet@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <copyright holder> ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.smbarbour.mcu.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.itadaki.bzip2.BZip2InputStream;

public class BinPatch {
	private static final int HEADER_SIZE = 32;

	public static void bspatch(Path origFile, Path newFile, Path patchFile) throws IOException {
		InputStream in = new BufferedInputStream(Files.newInputStream(origFile));
		byte[] diffBytes = new byte[(int) Files.size(patchFile)];
		InputStream patchStream = Files.newInputStream(patchFile);
		readFromStream(patchStream, diffBytes, 0, diffBytes.length);
		
		byte[] newBytes = bspatch(in, (int) Files.size(origFile), diffBytes);
		
		OutputStream outStream = Files.newOutputStream(newFile);
		outStream.write(newBytes);
		outStream.close();
	}
	
	private static byte[] bspatch(InputStream in, int size, byte[] diffBytes) throws IOException {
		byte[] oldBuf = new byte[size];
		readFromStream(in, oldBuf, 0, size);
		in.close();
		
		return bspatch(oldBuf, size, diffBytes, diffBytes.length);
	}

	private static byte[] bspatch(byte[] oldBuf, int oldsize, byte[] diffBytes, int diffSize) throws IOException {
		DataInputStream diffIn = new DataInputStream(new ByteArrayInputStream(diffBytes, 0, diffSize));
		
		diffIn.skip(8);
		long ctrlBlockLen = diffIn.readLong();
		long diffBlockLen = diffIn.readLong();
		int newsize = (int) diffIn.readLong();

		InputStream in;
		in = new ByteArrayInputStream(diffBytes, 0, diffSize);
		in.skip(HEADER_SIZE);
		DataInputStream ctrlBlockIn = new DataInputStream(new BZip2InputStream(in, false));
		
		in = new ByteArrayInputStream(diffBytes, 0, diffSize);
		in.skip(ctrlBlockLen + HEADER_SIZE);
		InputStream diffBlockIn = new BZip2InputStream(in, false);
		
		in = new ByteArrayInputStream(diffBytes, 0, diffSize);
		in.skip(diffBlockLen + ctrlBlockLen + HEADER_SIZE);
		InputStream extraBlockIn = new BZip2InputStream(in, false);
		
		byte[] newBuf = new byte[newsize];
		
		int oldpos = 0;
		int newpos = 0;
		int[] ctrl = new int[3];
		
		while (newpos < newsize) {
			for (int i = 0; i <= 2; i++) {
				ctrl[i] = ctrlBlockIn.readInt();
			}
			
			if (newpos + ctrl[0] > newsize) {
				throw new IOException("Corrupt patch.");
			}
			
			readFromStream(diffBlockIn, newBuf, newpos, ctrl[0]);
			
			for (int i = 0; i < ctrl[0]; i++) {
				if ((oldpos + i >= 0) && (oldpos + i < oldsize)) {
					newBuf[newpos + i] += oldBuf[oldpos + i];
				}
			}
			
			newpos += ctrl[0];
			oldpos += ctrl[0];
			
			if (newpos + ctrl[1] > newsize) {
				throw new IOException("Corrupt patch.");
			}
			
			readFromStream(extraBlockIn, newBuf, newpos, ctrl[1]);
			
			newpos += ctrl[1];
			oldpos += ctrl[2];
		}
		
		ctrlBlockIn.close();
		diffBlockIn.close();
		extraBlockIn.close();
		diffIn.close();
		
		return newBuf;
	}

	private static final void readFromStream(InputStream in, byte[] buf, int offset, int len) throws IOException {
		int totalBytesRead = 0;
		while (totalBytesRead < len) {
			int bytesRead = in.read(buf, offset + totalBytesRead, len - totalBytesRead);
			if (bytesRead < 0) {
				throw new IOException("Could not read expected number of bytes.");
			}
			totalBytesRead += bytesRead;
		}
	}
}
