type EmbeddingViewProps = {
  values: number[]
}

const chartWidth = 640
const chartHeight = 160
const half = chartHeight / 2

export function EmbeddingView({ values }: EmbeddingViewProps) {
  const peak = Math.max(...values.map((value) => Math.abs(value)), 1e-9)
  const slot = chartWidth / Math.max(values.length, 1)

  return (
    <section>
      <svg className="embedding-chart" viewBox={`0 0 ${chartWidth} ${chartHeight}`} role="img" aria-label="임베딩 막대 그래프">
        <line x1="0" y1={half} x2={chartWidth} y2={half} className="axis" />
        {values.map((value, index) => (
          <rect
            key={index}
            x={index * slot}
            y={barTop(value, peak)}
            width={Math.max(slot - 1, 1)}
            height={barSize(value, peak)}
            className={value < 0 ? 'bar-neg' : 'bar-pos'}
          >
            <title>{`차원 ${index}: ${value}`}</title>
          </rect>
        ))}
      </svg>
      <div className="embedding-table-wrap">
        <table>
          <thead>
            <tr>
              <th>차원</th>
              <th>값</th>
            </tr>
          </thead>
          <tbody>
            {values.map((value, index) => (
              <tr key={index}>
                <td>{index}</td>
                <td>{value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function barSize(value: number, peak: number): number {
  return (Math.abs(value) / peak) * (half - 8)
}

function barTop(value: number, peak: number): number {
  const size = barSize(value, peak)
  return value >= 0 ? half - size : half
}
