import { useState } from 'react'

// Campo de contraseña con botón para mostrar u ocultar lo escrito.
export default function CampoClave({ id, value, onChange, autoComplete, placeholder }) {
  const [visible, setVisible] = useState(false)
  return (
    <div className="campo-clave">
      <input
        id={id}
        type={visible ? 'text' : 'password'}
        value={value}
        onChange={onChange}
        autoComplete={autoComplete}
        placeholder={placeholder}
        required
      />
      <button
        type="button"
        className="ver-clave"
        onClick={() => setVisible((v) => !v)}
        aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
        aria-pressed={visible}
      >
        {visible ? 'Ocultar' : 'Ver'}
      </button>
    </div>
  )
}