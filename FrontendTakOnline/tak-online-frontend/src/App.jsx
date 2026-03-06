import { useMemo, useState } from 'react'
import './App.css'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const BOARD_SIZES = [3, 4, 5, 6, 7, 8]
const DIRECTIONS = {
  '-1,0': 'UP',
  '1,0': 'DOWN',
  '0,-1': 'LEFT',
  '0,1': 'RIGHT',
}

const emptyCreateForm = { playerName: '', boardSize: 5 }
const emptyJoinForm = { playerName: '', roomCode: '' }

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options.headers ?? {}) },
    ...options,
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `Error ${response.status}`)
  }

  return response.status === 204 ? null : response.json()
}

function App() {
  const [createForm, setCreateForm] = useState(emptyCreateForm)
  const [joinForm, setJoinForm] = useState(emptyJoinForm)
  const [room, setRoom] = useState(null)
  const [game, setGame] = useState(null)
  const [selectedPieceType, setSelectedPieceType] = useState('FLAT')
  const [dragFrom, setDragFrom] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [sparks, setSparks] = useState([])
  const [invertColors, setInvertColors] = useState(false)

  const activeName = room?.players?.at(-1)?.playerName ?? ''
  const activePlayer = useMemo(
    () => game?.players?.find((player) => player.playerName === activeName) ?? null,
    [game?.players, activeName],
  )

  const possibleDrops = useMemo(() => {
    if (!dragFrom || !game?.board) return []
    const moves = []
    const vectors = [
      [-1, 0],
      [1, 0],
      [0, -1],
      [0, 1],
    ]

    vectors.forEach(([dy, dx]) => {
      const row = dragFrom.row + dy
      const col = dragFrom.col + dx
      const inside = row >= 0 && row < game.boardSize && col >= 0 && col < game.boardSize
      if (inside) {
        moves.push({ row, col, direction: DIRECTIONS[`${dy},${dx}`] })
      }
    })

    return moves
  }, [dragFrom, game])

  const markSpark = () => {
    const id = crypto.randomUUID()
    const burst = Array.from({ length: 14 }).map((_, idx) => ({
      id: `${id}-${idx}`,
      x: `${15 + Math.random() * 70}%`,
      y: `${20 + Math.random() * 60}%`,
      delay: `${Math.random() * 240}ms`,
    }))
    setSparks((prev) => [...prev, ...burst])
    setTimeout(() => {
      setSparks((prev) => prev.filter((item) => !item.id.startsWith(id)))
    }, 900)
  }

  const withAsync = async (action) => {
    try {
      setLoading(true)
      setError('')
      await action()
      markSpark()
    } catch (err) {
      setError(err.message || 'Algo fue mal')
    } finally {
      setLoading(false)
    }
  }

  const createRoom = async (event) => {
    event.preventDefault()
    if (!createForm.playerName.trim()) {
      setError('Introduce un nickname para crear una sala.')
      return
    }

    await withAsync(async () => {
      const roomResponse = await request('/api/rooms', {
        method: 'POST',
        body: JSON.stringify({
          playerName: createForm.playerName.trim(),
          boardSize: Number(createForm.boardSize),
        }),
      })
      setRoom(roomResponse)
      setGame(null)
    })
  }

  const joinRoom = async (event) => {
    event.preventDefault()
    if (!joinForm.playerName.trim() || !joinForm.roomCode.trim()) {
      setError('Introduce nickname y código de sala para entrar.')
      return
    }

    await withAsync(async () => {
      const roomResponse = await request(`/api/rooms/${joinForm.roomCode.trim().toUpperCase()}/join`, {
        method: 'POST',
        body: JSON.stringify({ playerName: joinForm.playerName.trim() }),
      })
      setRoom(roomResponse)
      setGame(null)
    })
  }

  const loadGame = async () => {
    if (!room?.code) return
    await withAsync(async () => {
      try {
        const existing = await request(`/api/games/room/${room.code}`)
        setGame(existing)
      } catch {
        const created = await request(`/api/games/from-room/${room.code}`, { method: 'POST' })
        setGame(created)
      }
    })
  }

  const placePiece = async (row, col) => {
    if (!game || !activePlayer) return
    await withAsync(async () => {
      const updated = await request(`/api/games/${game.gameId}/moves/place`, {
        method: 'POST',
        body: JSON.stringify({
          playerId: activePlayer.playerId,
          row,
          col,
          pieceType: selectedPieceType,
        }),
      })
      setGame(updated)
    })
  }

  const moveOnePiece = async (toRow, toCol) => {
    if (!dragFrom || !game || !activePlayer) return
    const dy = toRow - dragFrom.row
    const dx = toCol - dragFrom.col
    const direction = DIRECTIONS[`${dy},${dx}`]
    if (!direction) return

    await withAsync(async () => {
      const updated = await request(`/api/games/${game.gameId}/moves/stack`, {
        method: 'POST',
        body: JSON.stringify({
          playerId: activePlayer.playerId,
          fromRow: dragFrom.row,
          fromCol: dragFrom.col,
          direction,
          pickupCount: 1,
          drops: [1],
        }),
      })
      setGame(updated)
      setDragFrom(null)
    })
  }

  const onDragStart = (event, row, col, topPiece) => {
    if (!activePlayer || game?.currentTurnColor !== activePlayer.color || topPiece?.color !== activePlayer.color) {
      event.preventDefault()
      return
    }
    setDragFrom({ row, col })
  }

  const canDropAt = (row, col) => possibleDrops.some((move) => move.row === row && move.col === col)
  const boardCells = game?.board?.cells ?? []

  return (
    <main className={`app ${invertColors ? 'inverted' : ''}`}>
      <header className="topbar">
        <h1>Tak Online</h1>
        <button className="ghost" onClick={() => setInvertColors((prev) => !prev)}>
          Invertir colores
        </button>
      </header>

      {!room && (
        <section className="panels">
          <form className="panel" onSubmit={createRoom}>
            <h2>Crear room</h2>
            <label>
              Nickname
              <input
                value={createForm.playerName}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, playerName: event.target.value }))}
                placeholder="ej: Blanca"
              />
            </label>
            <label>
              Tamaño tablero
              <select
                value={createForm.boardSize}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, boardSize: Number(event.target.value) }))}
              >
                {BOARD_SIZES.map((size) => (
                  <option key={size} value={size}>
                    {size}x{size}
                  </option>
                ))}
              </select>
            </label>
            <button type="submit" disabled={loading}>
              Crear sala
            </button>
          </form>

          <form className="panel" onSubmit={joinRoom}>
            <h2>Entrar en room</h2>
            <label>
              Nickname
              <input
                value={joinForm.playerName}
                onChange={(event) => setJoinForm((prev) => ({ ...prev, playerName: event.target.value }))}
                placeholder="ej: Negra"
              />
            </label>
            <label>
              Código de room
              <input
                value={joinForm.roomCode}
                onChange={(event) => setJoinForm((prev) => ({ ...prev, roomCode: event.target.value.toUpperCase() }))}
                placeholder="ABC123"
              />
            </label>
            <button type="submit" disabled={loading}>
              Entrar
            </button>
          </form>
        </section>
      )}

      {room && (
        <section className="room-area">
          <div className="room-info panel">
            <h2>Room {room.code}</h2>
            <p>Tablero: {room.boardSize}x{room.boardSize}</p>
            <ul>
              {room.players.map((player) => (
                <li key={player.playerId}>{player.playerName}</li>
              ))}
            </ul>
            <button onClick={loadGame} disabled={loading}>
              Entrar al tablero
            </button>
          </div>

          {game && (
            <div className="game-area panel">
              <div className="status">
                <strong>Turno: {game.currentTurnColor}</strong>
                <span>
                  Tú: {activePlayer?.playerName ?? 'espectador'} ({activePlayer?.color ?? '-'})
                </span>
              </div>

              <div className="reserves">
                {['FLAT', 'WALL', 'CAPSTONE'].map((pieceType) => (
                  <button
                    key={pieceType}
                    className={selectedPieceType === pieceType ? 'selected' : ''}
                    onClick={() => setSelectedPieceType(pieceType)}
                  >
                    {pieceType}
                  </button>
                ))}
              </div>

              <div className="board" style={{ gridTemplateColumns: `repeat(${game.boardSize}, 1fr)` }}>
                {boardCells.flatMap((rowCells, row) =>
                  rowCells.map((stack, col) => {
                    const top = stack.pieces.at(-1)
                    const drop = canDropAt(row, col)

                    return (
                      <button
                        key={`${row}-${col}`}
                        className={`cell ${drop ? 'drop-target' : ''}`}
                        draggable={Boolean(top)}
                        onDragStart={(event) => onDragStart(event, row, col, top)}
                        onDragOver={(event) => {
                          if (drop) event.preventDefault()
                        }}
                        onDrop={(event) => {
                          event.preventDefault()
                          if (drop) moveOnePiece(row, col)
                        }}
                        onClick={() => !top && placePiece(row, col)}
                      >
                        {top && <Piece piece={top} invertColors={invertColors} />}
                        {stack.pieces.length > 1 && (
                          <div className="stack-badge">{stack.pieces.length}</div>
                        )}
                        {stack.pieces.length > 0 && (
                          <div className="stack-preview">
                            {stack.pieces.map((piece, idx) => (
                              <span
                                key={`${idx}-${piece.color}`}
                                className={`bar ${piece.color.toLowerCase()} ${invertColors ? 'inverted' : ''}`}
                              />
                            ))}
                          </div>
                        )}
                      </button>
                    )
                  }),
                )}
              </div>
            </div>
          )}
        </section>
      )}

      {error && <p className="error">{error}</p>}
      {loading && <p className="loading">Sincronizando...</p>}

      <div className="spark-layer" aria-hidden>
        {sparks.map((spark) => (
          <span key={spark.id} className="spark" style={{ left: spark.x, top: spark.y, animationDelay: spark.delay }} />
        ))}
      </div>
    </main>
  )
}

function Piece({ piece, invertColors }) {
  const colorClass = piece.color === 'WHITE' ? (invertColors ? 'black' : 'white') : invertColors ? 'white' : 'black'
  const shapeClass = piece.color === 'WHITE' ? 'pentagon' : 'semicircle'
  return <span className={`piece ${shapeClass} ${colorClass}`} />
}

export default App
