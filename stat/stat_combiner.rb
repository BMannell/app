require 'writeexcel'
require 'csv'


def run
	experiments = 	ARGV[0].to_i || 1
	jobs = 			ARGV[1].to_i || 1

	for e in 0..(experiments-1) do

		workbook = WriteExcel.new("experiment#{e}/total.xls")

		for j in 0..(jobs-1) do

			worksheet  = workbook.add_worksheet

			data = CSV.read("experiment#{e}/job.#{j}.syreg-gen.stat", :col_sep => " ")

			data.each_index do |y|
				data[y].each_index do |x|
					worksheet.write(y, x, data[y][x].to_f)
				end
			end

		end

		worksheet = workbook.add_worksheet('total')

		for y in 0..199 do 
			worksheet.write(y,0,y)
			['B','C','D'].each do |x|
				formula = build_formula("AVERAGE",jobs, y, x)
				worksheet.write("#{x}#{y}", formula)
			end
		end
		workbook.close

	end
end


def build_formula(method,jobs,y,x)
	formula = "=#{method}("
	sheets = []
	for i in 1..3 do
		sheets.push('Sheet' + i.to_s + "\!#{x}#{y}")
	end
	formula += sheets.join(',')
	formula += ")"
	return formula
end

run