package com.raphydaphy.cutsceneapi.cutscene;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.util.math.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

public class Path
{
	private final List<Vector3f> points;
	private final List<Cubic> xCubics;
	private final List<Cubic> yCubics;
	private final List<Cubic> zCubics;

	public static Builder builder() {
	    return new Builder();
    }

	private Path(List<Vector3f> points, List<Cubic> xCubics, List<Cubic> yCubics, List<Cubic> zCubics)
	{
	    this.points = points;
	    this.xCubics = xCubics;
	    this.yCubics = yCubics;
	    this.zCubics = zCubics;
    }

	public List<Vector3f> getPoints()
	{
		return points;
	}

	public Vector3f getPoint(float position)
	{
		if (this.points.size() > 1)
		{
			position = position * xCubics.size();
			int cubicNum = (int) position;
			float cubicPos = (position - cubicNum);

			if (cubicNum < xCubics.size())
			{

				return new Vector3f(xCubics.get(cubicNum).eval(cubicPos),
						yCubics.get(cubicNum).eval(cubicPos),
						zCubics.get(cubicNum).eval(cubicPos));
			}
			return new Vector3f(this.points.get(this.points.size() - 1));
		}
		return new Vector3f(this.points.get(0));
	}

	public static class Builder {

        private final Vector<Vector3f> points = new Vector<>();
        private final Vector<Cubic> xCubics = new Vector<>();
        private final Vector<Cubic> yCubics = new Vector<>();
        private final Vector<Cubic> zCubics = new Vector<>();

        public Builder with(Vector3f point) {
            this.points.add(point);
            return this;
        }

        public Builder with(float x, float y, float z) {
            return with(new Vector3f(x, y, z));
        }

        public Path build() {
            return new Path(ImmutableList.copyOf(this.points),
                    Cubic.calcNatural(points, Vector3f::y, xCubics),
                    Cubic.calcNatural(points, Vector3f::y, yCubics),
                    Cubic.calcNatural(points, Vector3f::z, zCubics)
            );
        }
    }

	public static class Cubic
	{
		private float a, b, c, d;

		public Cubic(float a, float b, float c, float d)
		{
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}

		public float eval(float u)
		{
			return (((d * u) + c) * u + b) * u + a;
		}

        private static List<Cubic> calcNatural(List<Vector3f> valueCollection, Function<Vector3f, Float> valueExtractor, Vector<Cubic> cubics)
        {
            int num = valueCollection.size() - 1;
            if(num <= 0) {
                return ImmutableList.copyOf(cubics);
            }

            float[] gamma = new float[num + 1];
            float[] delta = new float[num + 1];
            float[] D = new float[num + 1];

            int i;
            gamma[0] = 1.0f / 2.0f;
            for (i = 1; i < num; i++)
            {
                gamma[i] = 1.0f / (4.0f - gamma[i - 1]);
            }
            gamma[num] = 1.0f / (2.0f - gamma[num - 1]);

            float p0 = valueExtractor.apply(valueCollection.get(0));
            float p1 = valueExtractor.apply(valueCollection.get(1));

            delta[0] = 3.0f * (p1 - p0) * gamma[0];
            for (i = 1; i < num; i++)
            {
                p0 = valueExtractor.apply(valueCollection.get(i - 1));
                p1 = valueExtractor.apply(valueCollection.get(i + 1));
                delta[i] = (3.0f * (p1 - p0) - delta[i - 1]) * gamma[i];
            }
            p0 = valueExtractor.apply(valueCollection.get(num - 1));
            p1 = valueExtractor.apply(valueCollection.get(num));

            delta[num] = (3.0f * (p1 - p0) - delta[num - 1]) * gamma[num];

            D[num] = delta[num];
            for (i = num - 1; i >= 0; i--)
            {
                D[i] = delta[i] - gamma[i] * D[i + 1];
            }

            Cubic[] arr = new Cubic[num];
            for (i = 0; i < num; i++)
            {
                p0 = valueExtractor.apply(valueCollection.get(i));
                p1 = valueExtractor.apply(valueCollection.get(i + 1));

                arr[i] = new Cubic(p0, D[i], 3 * (p1 - p0) - 2 * D[i] - D[i + 1], 2 * (p0 - p1) + D[i] + D[i + 1]);
            }
            return Arrays.asList(arr);
        }
	}
}