/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.charts.report

import com.excilys.ebi.gatling.charts.component.ComponentLibrary
import com.excilys.ebi.gatling.charts.computer.Computer.{ numberOfSuccessfulRequestsPerSecond, numberOfRequestsPerSecondAsList, numberOfFailedRequestsPerSecond }
import com.excilys.ebi.gatling.charts.config.ChartsFiles.globalTransactionsFile
import com.excilys.ebi.gatling.charts.series.Series
import com.excilys.ebi.gatling.charts.series.SharedSeries
import com.excilys.ebi.gatling.charts.template.TransactionsPageTemplate
import com.excilys.ebi.gatling.charts.util.Colors.{ toString, RED, GREEN, BLUE }
import com.excilys.ebi.gatling.charts.writer.TemplateWriter
import com.excilys.ebi.gatling.core.result.reader.DataReader

class TransactionsReportGenerator(runOn: String, dataReader: DataReader, componentLibrary: ComponentLibrary) extends ReportGenerator(runOn, dataReader, componentLibrary) {

	def generate = {

		// Get Data
		val allTransactionsData = numberOfRequestsPerSecondAsList(dataReader.dataIndexedByReceiveDateWithoutMillis)
		val failedTransactionsData = numberOfFailedRequestsPerSecond(dataReader.dataIndexedByReceiveDateWithoutMillis)
		val succeededTransactionsData = numberOfSuccessfulRequestsPerSecond(dataReader.dataIndexedByReceiveDateWithoutMillis)
		val pieData = ("Success", succeededTransactionsData.map(_._2).sum) :: ("Failures", failedTransactionsData.map(_._2).sum) :: Nil

		// Create series
		val allTransactions = new Series[Long, Int]("All requests", allTransactionsData, List(BLUE))
		val failedTransactions = new Series[Long, Int]("Failed requests", failedTransactionsData, List(RED))
		val succeededTransactions = new Series[Long, Int]("Succeeded requests", succeededTransactionsData, List(GREEN))
		val pieSeries = new Series[String, Int]("Repartition", pieData, List(GREEN, RED))

		// Create template
		val template = new TransactionsPageTemplate(componentLibrary.getTransactionsChartComponent(allTransactions, failedTransactions, succeededTransactions, pieSeries, SharedSeries.getAllActiveSessionsSeries))

		// Write template result to file
		new TemplateWriter(globalTransactionsFile(runOn)).writeToFile(template.getOutput)
	}
}