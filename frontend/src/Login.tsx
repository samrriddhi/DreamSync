import { FormEvent, useState } from 'react'
import { Command, LockKeyhole, Mail, ArrowRight, Sparkles } from 'lucide-react'
import { login } from './api'

export default function Login({onLogin}:{onLogin:()=>void}) {
 const [email,setEmail]=useState('')
 const [password,setPassword]=useState('')
 const [error,setError]=useState('')
 const [loading,setLoading]=useState(false)
 async function submit(e:FormEvent){e.preventDefault();setError('');setLoading(true);try{await login(email,password);onLogin()}catch{setError('We couldn’t sign you in. Check your email and password, then try again.')}finally{setLoading(false)}}
 return <main className="login-screen"><div className="login-art"><div className="login-brand"><span className="brand-mark"><Command size={19}/></span><b>dreamsync</b></div><div className="login-message"><span className="login-kicker"><Sparkles size={14}/> YOUR WORK, IN SYNC</span><h1>Make space for<br/><em>great work.</em></h1><p>Bring your projects, people and plans together. Less chasing updates, more moving forward.</p><div className="login-art-card"><div className="mini-line"/><div className="mini-board"><span/><span/><span/></div><div className="mini-caption">A clearer way to work, together.</div></div></div><div className="login-footer">Thoughtful collaboration starts here.</div></div><div className="login-panel"><form className="login-form" onSubmit={submit}><span className="eyebrow">WELCOME BACK</span><h2>Sign in to DreamSync</h2><p>Enter your details to access your workspace.</p><label className="field-label">Email address<div className="login-input"><Mail size={17}/><input type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@company.com" required/></div></label><label className="field-label">Password<div className="login-input"><LockKeyhole size={17}/><input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Enter your password" required/></div></label>{error&&<div className="login-error">{error}</div>}<button className="primary-btn login-submit" disabled={loading}>{loading?'Signing in...':'Sign in'} <ArrowRight size={16}/></button><div className="login-hint">Use an account already registered in your DreamSync database.</div></form><div className="login-panel-footer">© 2026 DreamSync · Built for teams that move together</div></div></main>
}
