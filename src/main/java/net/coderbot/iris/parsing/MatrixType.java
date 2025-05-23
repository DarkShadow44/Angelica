package net.coderbot.iris.parsing;

import kroppeb.stareval.function.Type;
import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class MatrixType<T> extends Type.ObjectType {
	public static final MatrixType<Matrix4f> MAT4 = new MatrixType<>("mat4", Matrix4f::new);
	public static MatrixType<Matrix2f> MAT2 = new MatrixType<>("mat2", Matrix2f::new);
	public static MatrixType<Matrix3f> MAT3 = new MatrixType<>("mat3", Matrix3f::new);
	final String name;

	public MatrixType(String name, Supplier<T> supplier) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
