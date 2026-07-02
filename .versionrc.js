module.exports = {
  types: [
    { type: 'feat', section: '✨ Features' },
    { type: 'fix', section: '🐛 Correcciones' },
    { type: 'docs', section: '📝 Documentación' },
    { type: 'ci', section: '⚙️ Infraestructura' },
    { type: 'refactor', section: '♻️ Refactor' },
    { type: 'perf', section: '⚡ Rendimiento' },
    { type: 'test', section: '🧪 Testing' },
    { type: 'chore', section: '🔧 Mantenimiento' },
    { type: 'style', section: '💄 Estilo' },
    { type: 'revert', section: '⏪ Reverts' },
  ],
  scripts: {
    postbump: './scripts/bump-version.sh',
  },
}
