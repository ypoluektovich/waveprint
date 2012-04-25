package org.shoushitsu.waveprint;

import msyu.util.collect.CollectionUtils;
import msyu.util.collect.Pair;
import org.shoushitsu.waveprint.db.DataAccessException;
import org.shoushitsu.waveprint.db.NoDataException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class MinhashTool {

	public static final String PERM = "perm";

	public static List<Permutation> generatePermutations(final int count, final int length) {
		final List<Permutation> list = new ArrayList<>(count);
		for (int i = 0; i < count; ++i) {
			list.add(Permutation.newRandom(length));
		}
		return list;
	}

	public static void main(final String[] args) {
		if (args.length == 0) {
			System.out.println("Usage:");
			System.out.format("\t%s ( -f <file> <count> <length> | -d <database> )%n", PERM);
			return;
		}
		switch (args[0]) {
			case PERM:
				processGeneratePermutations(args);
				break;
			default:
				System.out.println("Unknown command: " + args[0]);
				break;
		}
	}

	private static void processGeneratePermutations(final String[] args) {
		if (args.length != 3 && args.length != 5) {
			System.out.format(
					"Wrong number of arguments: expected 3 or 5, got %d%n" +
							"Usage: %s <count> <length> [ ( -f | -d ) <location> ]%n",
					args.length,
					PERM
			);
		}
		final String path = args[2];
		final int count;
		final int length;
		switch (args[1]) {
			case "-f":
				count = Integer.valueOf(args[3]);
				length = Integer.valueOf(args[4]);
				try {
					Files.write(
							Paths.get(path),
							CollectionUtils.map(
									generatePermutations(count, length),
									Permutation.DUMP_AS_STRING
							),
							Charset.forName("UTF-8")
					);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "-d":
				final Pair<Integer,Integer> minhashParams;
				try {
					minhashParams = DatabaseTool.getMinhashParams(path);
				} catch (DataAccessException | NoDataException e) {
					e.printStackTrace();
					return;
				}
				count = minhashParams.getFirst();
				length = minhashParams.getSecond();
				DatabaseTool.storePermutations(path, generatePermutations(count, length));
				break;
			default:
				System.out.println("Unsupported output designation: " + args[3]);
				return;
		}
	}
}
