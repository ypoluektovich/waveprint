package org.shoushitsu.waveprint;

import msyu.util.collect.IntArrayBuilder;
import msyu.util.collect.Pair;
import org.shoushitsu.waveprint.exceptions.sampleextractor.AudioConversionResultException;
import org.shoushitsu.waveprint.exceptions.sampleextractor.AudioConversionStartException;
import org.shoushitsu.waveprint.exceptions.sampleextractor.AudioConversionWaitException;
import org.shoushitsu.waveprint.exceptions.sampleextractor.ConvertedFileFormatException;
import org.shoushitsu.waveprint.exceptions.sampleextractor.ConvertedFileOpenException;
import org.shoushitsu.waveprint.exceptions.sampleextractor.ConvertedFileReadException;
import org.shoushitsu.waveprint.exceptions.sampleextractor.SampleExtractionException;
import org.shoushitsu.waveprint.exceptions.sampleextractor.TempFileCreationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class SampleExtractor {

	private final int mySampleRate;

	public SampleExtractor(final int sampleRate) {
		mySampleRate = sampleRate;
	}

	public int getSampleRate() {
		return mySampleRate;
	}

	public Pair<int[], Path> extract(@Nonnull final Path source)
			throws SampleExtractionException {
		return extract(source, null);
	}

	public Pair<int[], Path> extract(
			@Nonnull final Path source,
			@Nullable final Integer duration
	) throws SampleExtractionException {
		final Path convertedFile = convert(source, duration);
		try (final InputStream convertedStream = Files.newInputStream(convertedFile)) {
			return Pair.of(extractFromConverted(convertedStream), convertedFile);
		} catch (IOException e) {
			throw new ConvertedFileOpenException(e);
		}
	}

	private Path convert(@Nonnull final Path source, final Integer duration) throws SampleExtractionException {
		final Path dest;
		try {
			dest = Files.createTempFile(null, ".wav");
		} catch (IOException e) {
			throw new TempFileCreationException(e);
		}

		final Process process;
		try {
			final List<String> command = new ArrayList<>();
			command.addAll(Arrays.asList(
					"ffmpeg",
					"-i", source.toAbsolutePath().toString(), // input file
					"-vn",                                    // no video output
					"-acodec", "pcm_s16le",                   // PCM signed 16bit little-endian
					"-ac", "1",                               // 1 channel (mono)
					"-ar", Integer.toString(mySampleRate),    // sample rate
					"-y"                                      // overwrite destination
			));
			if (duration != null) {
				command.add("-t");                            // cut off output
				command.add(String.valueOf(duration));        // after N seconds
			}
			command.add(dest.toString());                     // output file
			process = new ProcessBuilder(command).start();
			// todo: intercept output?
		} catch (IOException e) {
			throw new AudioConversionStartException(e);
		}

		final int exitCode;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			// todo: protection against accidental interrupts
			process.destroy();
			throw new AudioConversionWaitException(e);
		}

		if (exitCode != 0) {
			throw new AudioConversionResultException(exitCode);
		}

		return dest;
	}

	public int[] extractFromConverted(@Nonnull final InputStream convertedAudio) throws SampleExtractionException {
		try (
				final InputStream bufferedStream = new BufferedInputStream(convertedAudio);
				final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream)
		) {
			// todo: remove cast / support long?
			final int frameLength = (int) audioInputStream.getFrameLength();
			final IntArrayBuilder samples = new IntArrayBuilder(frameLength);

			// assuming frame size = 2
			final byte[] buffer = new byte[2];
			boolean eof = false;
			while (!eof) {
				final int read = audioInputStream.read(buffer);
				if (read == -1) {
					eof = true;
				} else {
					// little endian
					final int sample = (((int) buffer[0]) & 0xff) |
							(((int) buffer[1]) << 8);
					samples.append(sample);
				}
			}
			// todo: add check for samples.size() != frameLength
			return samples.toIntArray();
		} catch (UnsupportedAudioFileException e) {
			throw new ConvertedFileFormatException(e);
		} catch (IOException e) {
			throw new ConvertedFileReadException(e);
		}
	}

}
