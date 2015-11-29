/*
 * Copyright (c) 2015 Villu Ruusmann
 *
 * This file is part of JPMML-Spark
 *
 * JPMML-Spark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Spark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Spark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.spark;

import java.io.File;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.jpmml.evaluator.Evaluator;

public class Main {

	static
	public void main(String... args) throws Exception {

		if(args.length != 3){
			System.err.println("Usage: java " + Main.class.getName() + " <PMML file> <Input CSV file> <Output directory>");

			System.exit(-1);
		}

		Evaluator evaluator = PMMLFunctionUtil.createEvaluator(new File(args[0]));

		PMMLFunction pmmlFunction = new PMMLFunction(evaluator);

		SparkConf conf = new SparkConf();

		JavaSparkContext sparkContext = new JavaSparkContext(conf);

		SQLContext sqlContext = new SQLContext(sparkContext);

		DataFrameReader reader = sqlContext.read()
			.format("com.databricks.spark.csv")
			.option("header", "true");

		DataFrame inputDataFrame = reader.load(args[1]);

		JavaRDD<Row> inputRDD = inputDataFrame.toJavaRDD();

		JavaRDD<Row> outputRDD = inputRDD.map(pmmlFunction);

		DataFrame outputDataFrame = sqlContext.createDataFrame(outputRDD, pmmlFunction.getOutputSchema());

		DataFrameWriter writer = outputDataFrame.write()
			.format("com.databricks.spark.csv")
		    .option("header", "true");

		writer.save(args[2]);
	}
}